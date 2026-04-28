package zw.co.nbs.returnstrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zw.co.nbs.returnstrackingsystem.config.StorageProperties;
import zw.co.nbs.returnstrackingsystem.domain.enums.DocumentStatus;
import zw.co.nbs.returnstrackingsystem.dtos.request.DocumentRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.DocumentResponse;
import zw.co.nbs.returnstrackingsystem.convertor.DocumentObjectMapper;
import zw.co.nbs.returnstrackingsystem.domain.Document;
import zw.co.nbs.returnstrackingsystem.dtos.response.PagedResponse;
import zw.co.nbs.returnstrackingsystem.service.DocStoreService;
import zw.co.nbs.returnstrackingsystem.service.impl.DocumentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static zw.co.nbs.returnstrackingsystem.util.AppUtil.getPreviewContentType;
import static zw.co.nbs.returnstrackingsystem.util.AppUtil.isPreviewSupported;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document API", description = "CRUD operations for documents")
public class DocumentController {

    private final DocumentServiceImpl documentService;
    private final DocumentObjectMapper documentObjectMapper;
    private final DocStoreService docStoreService;
    private final StorageProperties storageProperties;

    @Operation(summary = "Get all documents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<DocumentResponse>> getAllDocuments(
            @Parameter(description = "Pagination information")
            @PageableDefault (direction = DESC,sort="id") Pageable pageable) {
        var page = documentService.getAllDocuments(pageable)
                .map(documentObjectMapper::toDocumentResponse);

        return ResponseEntity.ok(
                PagedResponse.<DocumentResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }

    @Operation(summary = "Get document by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @Parameter(description = "ID of the document to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(documentObjectMapper
                .toDocumentResponse(documentService.getDocumentById(id)));
    }

    @Operation(summary = "Update document by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @Parameter(description = "ID of the document to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated document payload", required = true)
            @Valid @RequestBody DocumentRequest documentDTO) {
        return ResponseEntity.ok(documentObjectMapper
                .toDocumentResponse(documentService.updateDocument(id, documentDTO)));
    }

    @Operation(summary = "Approve uploaded document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document approved successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PatchMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> approveDocument(
            @Parameter(description = "ID of the document to approve")
            @PathVariable("documentId") Long documentId,
            @Parameter(description = "Document status")
            @RequestParam(value = "documentStatus",defaultValue = "VERIFIED") DocumentStatus documentStatus){
        return ResponseEntity.ok(
                documentService
                        .approveDocument(
                                documentId,
                                documentStatus
                        ));
    }

    @Operation(summary = "Delete document by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID of the document to delete", required = true)
            @PathVariable Long id) {
        return documentService.deleteDocument(id)
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping("{documentId}/view")
    @Operation(summary = "Smart view document",
            description = "Preview if supported, download if not supported")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully viewed document"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Resource> smartViewDocument(
            @Parameter(description = "Document ID", required = true, example = "1")
            @PathVariable Long documentId) throws IOException {

        Document document = documentService.getDocumentById(documentId);
        Resource resource = docStoreService.getFile(document.getStorageUrl());
        String contentType = getPreviewContentType(document.getContentType());

        boolean canPreview = isFileTypeAllowed(document.getFileType());
        String contentDisposition = canPreview ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition + "; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
    private boolean isFileTypeAllowed(String contentType) {
        if (contentType == null) return false;

        List<String> allowedTypes = storageProperties.getAllowedFileTypes();
        return allowedTypes.contains(contentType);
    }

}
