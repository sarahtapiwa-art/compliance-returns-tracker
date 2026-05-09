package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.repository.UserRepository;
import returnstrackingsystem.service.UserService;

/**
 * createdBy romeo
 * createdDate 3/12/2025
 * createdTime 10:16
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findUserByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RecordNotFoundException("User not found with email: %s"
                        .formatted(email)));
    }
}
