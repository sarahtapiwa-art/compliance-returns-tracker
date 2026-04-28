package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.*;
import zw.co.nbs.returnstrackingsystem.dtos.request.DocumentRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.DocumentResponse;
import zw.co.nbs.returnstrackingsystem.domain.Document;

@Mapper(componentModel = "spring")
public interface DocumentObjectMapper {

    Document toDocument(DocumentRequest documentDTO);
    DocumentResponse toDocumentResponse(Document document);
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDocument(DocumentRequest documentDTO, @MappingTarget Document document);

}
