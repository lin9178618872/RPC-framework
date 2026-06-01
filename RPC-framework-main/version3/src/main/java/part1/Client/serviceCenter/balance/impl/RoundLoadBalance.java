package part1.Client.serviceCenter.balance.impl;

import part1.Client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundLoadBalance implements LoadBalance {

    private final ConcurrentHashMap<String, AtomicInteger> indexMap = new ConcurrentHashMap<>();

    @Override
    public String select(String serviceName, List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new RuntimeException("No available provider for service: " + serviceName);
        }

        AtomicInteger index = indexMap.computeIfAbsent(serviceName, k -> new AtomicInteger(0));

        int current = Math.abs(index.getAndIncrement());
        return addressList.get(current % addressList.size());
    }
}