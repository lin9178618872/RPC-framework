package part1.Client;

import part1.Client.proxy.ClientProxy;
import part1.common.pojo.User;
import part1.common.service.UserService;

/**
 * RPC client test.
 */
public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);

        User newUser = User.builder()
                .id(100L)
                .username("wxx")
                .email("wxx@example.com")
                .age(20)
                .active(true)
                .build();

        Long createdId = proxy.createUser(newUser);
        System.out.println("created user id = " + createdId);

        User user = proxy.getUserById(createdId);
        System.out.println("from server get user = " + user);

        boolean updated = proxy.updateUserEmail(createdId, "new-email@example.com");
        System.out.println("update email result = " + updated);

        boolean deactivated = proxy.deactivateUser(createdId);
        System.out.println("deactivate user result = " + deactivated);
    }
}
