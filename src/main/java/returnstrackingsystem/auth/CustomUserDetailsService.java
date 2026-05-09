package returnstrackingsystem.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 08:45
 * projectName compliance-returns-tracker
 **/

public interface CustomUserDetailsService extends UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    UserDetails loadUserByEmail(String email) throws UsernameNotFoundException;
}
