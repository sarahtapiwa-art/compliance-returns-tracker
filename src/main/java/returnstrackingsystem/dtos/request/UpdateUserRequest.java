package returnstrackingsystem.dtos.request;

import returnstrackingsystem.domain.enums.Role;
import java.util.Set;

public record UpdateUserRequest(
        String username,
        String email,
        Long departmentId,
        Set<Role> roles
) {}
