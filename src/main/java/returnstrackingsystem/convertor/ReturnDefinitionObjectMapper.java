package returnstrackingsystem.convertor;

import org.mapstruct.*;
import returnstrackingsystem.dtos.response.ReturnDefinitionResponse;
import returnstrackingsystem.domain.ReturnDefinition;

@Mapper(componentModel = "spring")
public interface ReturnDefinitionObjectMapper {
    ReturnDefinitionResponse toReturnDefinitionResponse(ReturnDefinition returnDefinition);

}
