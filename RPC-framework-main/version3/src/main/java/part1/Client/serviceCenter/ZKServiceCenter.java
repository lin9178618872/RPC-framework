package part1.Client.serviceCenter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import part1.Client.cache.ServiceCache;
import part1.Client.serviceCenter.ZkWatcher.WatchZK;
import part1.Client.serviceCenter.balance.LoadBalance;
import part1.Client.serviceCenter.balance.impl.RoundLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

public class ZKServiceCenter implements ServiceCenter {

    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String ROOT_PATH = "MyRPC";

    private final CuratorFramework client;
    private final ServiceCache serviceCache;
    private final LoadBalance loadBalance;

    public ZKServiceCenter() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        this.client = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace(ROOT_PATH)
                .build();

        this.client.start();

        this.serviceCache = new ServiceCache();
        this.loadBalance = new RoundLoadBalance();

        new WatchZK(client, serviceCache).watch();

        System.out.println("Zookeeper connected, service cache started");
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<String> addressList = serviceCache.getServiceList(serviceName);

            if (addressList == null || addressList.isEmpty()) {
                addressList = client.getChildren().forPath("/" + serviceName);

                for (String address : addressList) {
                    serviceCache.addService(serviceName, address);
                }
            }

            String selectedAddress = loadBalance.select(serviceName, addressList);

            return parseAddress(selectedAddress);

        } catch (Exception e) {
            throw new RuntimeException("Service discovery failed: " + serviceName, e);
        }
    }

    private InetSocketAddress parseAddress(String address) {
        String[] parts = address.split(":");
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }
}