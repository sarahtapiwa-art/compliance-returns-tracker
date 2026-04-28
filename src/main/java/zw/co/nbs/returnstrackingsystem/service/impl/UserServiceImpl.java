package zw.co.nbs.returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.exception.RecordNotFoundException;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;
import zw.co.nbs.returnstrackingsystem.service.UserService;

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
