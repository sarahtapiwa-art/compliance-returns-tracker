package returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import returnstrackingsystem.domain.enums.DocumentStatus;
import returnstrackingsystem.dtos.request.DocumentRequest;
import returnstrackingsystem.domain.Document;
import returnstrackingsystem.dtos.response.DocumentResponse;

public interface DocumentService {
    Document createDocument(DocumentRequest documentDTO);
    Document getDocumentById(Long id);
    Document updateDocument(Long id, DocumentRequest documentDTO);
    Page<Document> getAllDocuments(Pageable pageable);
    boolean deleteDocument(Long id);
    DocumentResponse approveDocument(Long id, DocumentStatus  documentStatus);
}
