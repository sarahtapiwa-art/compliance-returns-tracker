package zw.co.nbs.returnstrackingsystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import zw.co.nbs.returnstrackingsystem.domain.Department;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.domain.enums.Role;
import zw.co.nbs.returnstrackingsystem.repository.DepartmentRepository;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;

import java.util.Set;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 11:16
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;

    @Value("${system.admin.username}")
    private String sysAdminUsername;
    @Value("${system.admin.password}")
    private String sysAdminPassword;
    @Value("${system.admin.email}")
    private String systemAdminEmail;

    @Value("${app.default-department.name}")
    private String defaultDepartment;
    @Value("${app.default-department.hod-email}")
    private String hodEmail;
    @Value("${app.default-department.escalation-email}")
    private String escalationEmail;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername(sysAdminUsername).isEmpty()) {
            Department department = departmentRepository.findByDepartmentName(defaultDepartment)
                    .orElseGet(() -> departmentRepository.save(
                            Department.builder()
                                    .departmentName(defaultDepartment)
                                    .headOfDepartmentEmail(hodEmail)
                                    .escalationEmail(escalationEmail)
                                    .build()
                    ));
            var superAdmin = User.builder()
                    .username(sysAdminUsername)
                    .password(passwordEncoder.encode(sysAdminPassword))
                    .roles(Set.of(Role.SUPER_SYSTEM_ADMIN))
                    .email(systemAdminEmail)
                    .department(department)
                    .build();

            var savedUser = userRepository.save(superAdmin);

            log.info("SUPER_SYSTEM_ADMIN created with username: {}", savedUser.getUsername());
        }
    }
}

