package returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import returnstrackingsystem.domain.TaskTrack;
import returnstrackingsystem.dtos.response.TaskTrackResponse;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 14:50
 * projectName compliance-returns-tracker
 **/

@Mapper(componentModel = "spring")
public interface TaskTrackObjectMapper {

    TaskTrackResponse toTaskTrackResponse(TaskTrack taskTrack);
}
