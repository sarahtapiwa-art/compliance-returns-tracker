package returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import returnstrackingsystem.domain.NotificationLog;
import returnstrackingsystem.dtos.response.NotificationLogResponse;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 13:59
 * projectName compliance-returns-tracker
 **/

@Mapper(componentModel = "spring")
public interface NotificationLogObjectMapper {
    NotificationLogResponse toNotificationLog(NotificationLog notificationLog);
}
