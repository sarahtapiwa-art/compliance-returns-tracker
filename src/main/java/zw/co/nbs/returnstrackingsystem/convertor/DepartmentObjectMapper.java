package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.*;
import zw.co.nbs.returnstrackingsystem.dtos.request.DepartmentRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.DepartmentResponse;
import zw.co.nbs.returnstrackingsystem.domain.Department;

@Mapper(componentModel = "spring")
public interface DepartmentObjectMapper {

    Department toDepartment(DepartmentRequest departmentDTO);
    DepartmentResponse toDepartmentResponse(Department department);
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDepartment(DepartmentRequest departmentDTO, @MappingTarget Department department);

}
