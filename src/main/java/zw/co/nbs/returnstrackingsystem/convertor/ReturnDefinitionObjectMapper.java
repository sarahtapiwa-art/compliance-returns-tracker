package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.*;
import zw.co.nbs.returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.ReturnDefinitionResponse;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;

@Mapper(componentModel = "spring")
public interface ReturnDefinitionObjectMapper {
    ReturnDefinitionResponse toReturnDefinitionResponse(ReturnDefinition returnDefinition);

}
