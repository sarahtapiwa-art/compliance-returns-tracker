package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zw.co.nbs.returnstrackingsystem.domain.enums.DocumentStatus;
import zw.co.nbs.returnstrackingsystem.dtos.request.DocumentRequest;
import zw.co.nbs.returnstrackingsystem.domain.Document;
import zw.co.nbs.returnstrackingsystem.dtos.response.DocumentResponse;

public interface DocumentService {
    Document createDocument(DocumentRequest documentDTO);
    Document getDocumentById(Long id);
    Document updateDocument(Long id, DocumentRequest documentDTO);
    Page<Document> getAllDocuments(Pageable pageable);
    boolean deleteDocument(Long id);
    DocumentResponse approveDocument(Long id, DocumentStatus  documentStatus);
}
