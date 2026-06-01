package part1.common.service;

import part1.common.pojo.User;

public interface UserService {

    User getUserById(Long id);

    Long createUser(User user);

    boolean updateUserEmail(Long id, String email);

    boolean deactivateUser(Long id);
}