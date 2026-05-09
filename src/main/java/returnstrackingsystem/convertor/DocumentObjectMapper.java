package returnstrackingsystem.convertor;

import org.mapstruct.*;
import returnstrackingsystem.dtos.request.DocumentRequest;
import returnstrackingsystem.dtos.response.DocumentResponse;
import returnstrackingsystem.domain.Document;

@Mapper(componentModel = "spring")
public interface DocumentObjectMapper {

    Document toDocument(DocumentRequest documentDTO);
    DocumentResponse toDocumentResponse(Document document);
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDocument(DocumentRequest documentDTO, @MappingTarget Document document);

}
