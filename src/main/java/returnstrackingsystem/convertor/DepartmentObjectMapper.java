package returnstrackingsystem.convertor;

import org.mapstruct.*;
import returnstrackingsystem.dtos.request.DepartmentRequest;
import returnstrackingsystem.dtos.response.DepartmentResponse;
import returnstrackingsystem.domain.Department;

@Mapper(componentModel = "spring")
public interface DepartmentObjectMapper {

    Department toDepartment(DepartmentRequest departmentDTO);
    DepartmentResponse toDepartmentResponse(Department department);
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDepartment(DepartmentRequest departmentDTO, @MappingTarget Department department);

}
