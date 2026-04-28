package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import zw.co.nbs.returnstrackingsystem.domain.NotificationLog;
import zw.co.nbs.returnstrackingsystem.dtos.response.NotificationLogResponse;

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
