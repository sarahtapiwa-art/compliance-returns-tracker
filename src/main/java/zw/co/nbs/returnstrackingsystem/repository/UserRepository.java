package zw.co.nbs.returnstrackingsystem.repository;

import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zw.co.nbs.returnstrackingsystem.domain.User;

import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 08:49
 * projectName compliance-returns-tracker
 **/

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.username = :username")
    Optional<User> findByUsernameWithDepartment(@Param("username") String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(@Email String email);

    Optional<User> findByUsername(String name);
}
