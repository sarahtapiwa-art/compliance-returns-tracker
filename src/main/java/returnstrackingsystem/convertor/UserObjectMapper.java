package returnstrackingsystem.convertor;

import org.mapstruct.Mapper;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.dtos.response.UserResponse;

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
