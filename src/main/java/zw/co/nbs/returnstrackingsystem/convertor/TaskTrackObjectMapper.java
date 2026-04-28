package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import zw.co.nbs.returnstrackingsystem.domain.TaskTrack;
import zw.co.nbs.returnstrackingsystem.dtos.response.TaskTrackResponse;

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
