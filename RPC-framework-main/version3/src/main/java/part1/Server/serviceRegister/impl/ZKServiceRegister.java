package part1.Server.serviceRegister.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import part1.Server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class ZKServiceRegister implements ServiceRegister {

    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String ROOT_PATH = "MyRPC";

    private final CuratorFramework client;

    public ZKServiceRegister() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        this.client = CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .sessionTimeoutMs(40000)
                .retryPolicy(retryPolicy)
                .namespace(ROOT_PATH)
                .build();

        this.client.start();
        try {
            if (!this.client.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Connect to Zookeeper timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while connecting to Zookeeper", e);
        }

        System.out.println("Zookeeper connected");
    }

    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            String servicePath = "/" + serviceName;

            if (client.checkExists().forPath(servicePath) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(servicePath);
            }

            String address = serviceAddress.getHostName() + ":" + serviceAddress.getPort();
            String addressPath = servicePath + "/" + address;

            if (client.checkExists().forPath(addressPath) == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(addressPath);
            }

            System.out.println("Register to Zookeeper: " + serviceName + " -> " + address);

        } catch (Exception e) {
            throw new RuntimeException("Register service failed: " + serviceName, e);
        }
    }
}
