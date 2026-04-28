package zw.co.nbs.returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.domain.NotificationLog;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.exception.RecordNotFoundException;
import zw.co.nbs.returnstrackingsystem.repository.NotificationLogRepository;
import zw.co.nbs.returnstrackingsystem.repository.UserRepository;
import zw.co.nbs.returnstrackingsystem.service.NotificationLogService;

import java.security.Principal;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 13:54
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLogServiceImpl implements NotificationLogService {

    private final NotificationLogRepository notificationLogRepository;
    private final UserRepository userRepository;

    @Value("${superadmin.role}")
    private String superAdminUserRole;

    @Override
    public Page<NotificationLog> getNotificationLogs(Principal currentUser, Pageable pageable) {
        log.debug("Retrieving all notification logs");

        var user = userRepository.findByUsername(currentUser.getName())
                .orElseThrow(() -> new RecordNotFoundException("User not found"));

        boolean isSuperAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority()
                        .equals(superAdminUserRole));

        if (isSuperAdmin) {
           return notificationLogRepository.findAll(pageable);
        }

        String departmentName = user.getDepartment().getDepartmentName();
        return notificationLogRepository
                .findAllBySubmission_ReturnDefinition_Department_DepartmentName(
                        departmentName,
                        pageable
                );
    }
}
