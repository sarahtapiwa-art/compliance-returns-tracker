package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import returnstrackingsystem.domain.enums.DocumentStatus;
import returnstrackingsystem.dtos.request.DocumentRequest;
import returnstrackingsystem.dtos.response.DocumentResponse;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.convertor.DocumentObjectMapper;
import returnstrackingsystem.domain.Document;
import returnstrackingsystem.repository.DocumentRepository;
import returnstrackingsystem.service.DocumentService;
import returnstrackingsystem.service.EmailService;

import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentObjectMapper documentObjectMapper;
    private final EmailService emailService;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    public Document createDocument(DocumentRequest documentDTO) {
        requireNonNull(documentDTO, "Document DTO cannot be null");

        log.info("Creating new document");
        var document = documentObjectMapper.toDocument(documentDTO);

        log.info("Saving document");
        return documentRepository.save(document);
    }

    @Override
    public Page<Document> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Document getDocumentById(Long id) {
        requireNonNull(id, "Document id cannot be null");

        log.info("Getting document with id: {}", id);
        return documentRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException(
                        format("Document with id: %d, not found", id)
                )
        );
    }

    public Document updateDocument(Long id, DocumentRequest documentDTO) {
        requireNonNull(documentDTO, "Document DTO cannot be null");
        requireNonNull(id, "Document id cannot be null");

        var document = getDocumentById(id);
        log.info("Updating document with id: {}", id);
        documentObjectMapper.updateDocument(documentDTO, document);
        log.info("Saving updated document");
        return documentRepository.save(document);
    }

    public boolean deleteDocument(Long id) {
        return documentRepository.findById(id).map(document -> {
            documentRepository.delete(document);
            return true;
        }).orElse(false);
    }

    @Override
    public DocumentResponse approveDocument(Long id, DocumentStatus documentStatus) {
        requireNonNull(id, "Document id cannot be null");
        requireNonNull(documentStatus, "Document status cannot be null");

        var  document = getDocumentById(id);
        document.setStatus(documentStatus);
        String to = document.getSubmission()
                .getReturnDefinition()
                .getResponsiblePerson()
                .getEmail();
        String body;
        String reportTitle = document.getSubmission()
                .getReturnDefinition()
                .getTitle();
        if (Objects.equals(documentStatus, DocumentStatus.REJECTED)){
            log.info("Rejected document with id: {}", id);
            document.setStatus(DocumentStatus.REJECTED);
            documentRepository.delete(document);
            body = emailService.buildRejectedNotificationEmail(reportTitle);
            emailService.send(
                    from,
                    to,
                    null,
                    "Document Rejected",
                    body
            );
            return documentObjectMapper.toDocumentResponse(document);
        }
        log.info("Approving document with id: {}", id);
        documentRepository.save(document);
        body = emailService.buildVerifiedNotificationEmail(reportTitle);
        emailService.send(
                from,
                to,
                null,
                "Document Verified",
                body
        );
        return documentObjectMapper.toDocumentResponse(document);
    }
}