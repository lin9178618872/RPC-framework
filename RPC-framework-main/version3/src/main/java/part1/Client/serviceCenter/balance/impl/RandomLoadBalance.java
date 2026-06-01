package part1.Client.serviceCenter.balance.impl;

import part1.Client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance implements LoadBalance {

    @Override
    public String select(String serviceName, List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new RuntimeException("No available provider for service: " + serviceName);
        }

        int index = ThreadLocalRandom.current().nextInt(addressList.size());
        return addressList.get(index);
    }
}