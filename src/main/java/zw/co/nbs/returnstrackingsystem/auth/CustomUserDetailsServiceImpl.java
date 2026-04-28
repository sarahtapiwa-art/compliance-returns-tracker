package zw.co.nbs.returnstrackingsystem.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.customvalidation.NbsEmail;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;

import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 08:49
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService{

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        requireNonNull(username, "Username must not be null.");
        log.info("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        format("No user found with username: '%s'.", username)
                ));
    }

    @Override
    public UserDetails loadUserByEmail(@NbsEmail String email) throws UsernameNotFoundException {
        requireNonNull(email, "Email must not be null.");
        log.info("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        format("No user found with email: '%s'.", email)
                ));
    }
}
