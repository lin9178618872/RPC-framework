package part1.common.service.Impl;

import part1.common.pojo.User;
import part1.common.service.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceImpl implements UserService {

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();

    @Override
    public User getUserById(Long id) {
        return userStore.get(id);
    }

    @Override
    public Long createUser(User user) {
        userStore.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public boolean updateUserEmail(Long id, String email) {
        User user = userStore.get(id);

        if (user == null) {
            return false;
        }

        user.setEmail(email);
        return true;
    }

    @Override
    public boolean deactivateUser(Long id) {
        User user = userStore.get(id);

        if (user == null) {
            return false;
        }

        user.setActive(false);
        return true;
    }
}