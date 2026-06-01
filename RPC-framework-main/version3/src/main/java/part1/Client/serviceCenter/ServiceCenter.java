package part1.Client.serviceCenter;

import java.net.InetSocketAddress;


public interface ServiceCenter {
    InetSocketAddress serviceDiscovery(String serviceName);
}
