package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import zw.co.nbs.returnstrackingsystem.domain.User;
import zw.co.nbs.returnstrackingsystem.dtos.response.UserResponse;

/**
 * createdBy romeo
 * createdDate 28/8/2025
 * createdTime 12:00
 * projectName compliance-returns-tracker
 **/

@Mapper(componentModel = "spring")
public interface UserObjectMapper {
    UserResponse toUserResponse(User user);
}
