package returnstrackingsystem.auth;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetailsService extends UserDetailsService {
    UserDetails loadUserByEmail(String email);
}
