package returnstrackingsystem.service;

import returnstrackingsystem.domain.User;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 10:15
 * projectName compliance-returns-tracker
 **/

public interface UserService {
    User findUserByEmail(String email);

}
