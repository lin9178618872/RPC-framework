package part1.Client.serviceCenter.balance;

import java.util.List;

public interface LoadBalance {

    String select(String serviceName, List<String> addressList);
}