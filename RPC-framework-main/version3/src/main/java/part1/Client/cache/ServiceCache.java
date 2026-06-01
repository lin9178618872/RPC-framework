package part1.Client.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceCache {

    private final Map<String, List<String>> serviceMap = new ConcurrentHashMap<>();

    public void addService(String serviceName, String address) {
        List<String> addressList =
                serviceMap.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>());

        if (!addressList.contains(address)) {
            addressList.add(address);
            System.out.println("Add service cache: " + serviceName + " -> " + address);
        }
    }

    public void removeService(String serviceName, String address) {
        List<String> addressList = serviceMap.get(serviceName);

        if (addressList == null) {
            return;
        }

        addressList.remove(address);

        if (addressList.isEmpty()) {
            serviceMap.remove(serviceName);
        }

        System.out.println("Remove service cache: " + serviceName + " -> " + address);
    }

    public void updateService(String serviceName, String oldAddress, String newAddress) {
        removeService(serviceName, oldAddress);
        addService(serviceName, newAddress);
    }

    public List<String> getServiceList(String serviceName) {
        return serviceMap.get(serviceName);
    }
}