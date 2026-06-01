package part1.Client.serviceCenter.balance.impl;

import part1.Client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistencyHashBalance implements LoadBalance {

    private static final int VIRTUAL_NODE_COUNT = 10;

    @Override
    public String select(String serviceName, List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new RuntimeException("No available provider for service: " + serviceName);
        }

        TreeMap<Integer, String> hashRing = new TreeMap<>();

        for (String address : addressList) {
            for (int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
                String virtualNode = address + "#VN" + i;
                hashRing.put(hash(virtualNode), address);
            }
        }

        int requestHash = hash(serviceName + System.nanoTime());

        SortedMap<Integer, String> tailMap = hashRing.tailMap(requestHash);

        Integer selectedKey = tailMap.isEmpty()
                ? hashRing.firstKey()
                : tailMap.firstKey();

        return hashRing.get(selectedKey);
    }

    private int hash(String value) {
        final int p = 16777619;
        int hash = (int) 2166136261L;

        for (int i = 0; i < value.length(); i++) {
            hash = (hash ^ value.charAt(i)) * p;
        }

        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        return hash & 0x7fffffff;
    }
}