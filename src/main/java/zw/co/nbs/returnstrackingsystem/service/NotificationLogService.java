package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zw.co.nbs.returnstrackingsystem.domain.NotificationLog;

import java.security.Principal;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 13:51
 * projectName compliance-returns-tracker
 **/

public interface NotificationLogService {
    Page<NotificationLog> getNotificationLogs(Principal principal, Pageable pageable);
}
