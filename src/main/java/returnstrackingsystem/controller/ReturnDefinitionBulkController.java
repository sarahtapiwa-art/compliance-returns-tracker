package returnstrackingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.dtos.request.BulkReturnDefinitionRequest;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.service.ReturnDefinitionBulkService;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 11:14
 * projectName compliance-returns-tracker
 **/

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/return-definition/bulk")
@Tag(name = "ReturnDefinition Bulk Upload", description = "Endpoints for bulk upload of ReturnDefinition")
public class ReturnDefinitionBulkController {

    private final ReturnDefinitionBulkService bulkService;

    @PostMapping
    @Operation(summary = "Bulk create regulatory report definitions")
    public ResponseEntity<BulkUploadResponse> bulkCreateReports(
            @Valid @RequestBody BulkReturnDefinitionRequest bulkRequest) {

        log.info("Received bulk create request for {} reports", bulkRequest.getReports().size());
        BulkUploadResponse response = bulkService.bulkCreateReports(bulkRequest.getReports());
        return ResponseEntity.status(getHttpStatus(response)).body(response);
    }

    @PostMapping(value = "/attach/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk upload regulatory report definitions from Excel file")
    public ResponseEntity<BulkUploadResponse> bulkUploadFromExcel(
            @Parameter(description = "Excel file with report definitions")
            @RequestParam("file") MultipartFile file) {

        log.info("Received bulk upload request from Excel file: {}", file.getOriginalFilename());

        if (!isExcelFile(file)) {
            throw new IllegalArgumentException("Only Excel files are allowed");
        }

        BulkUploadResponse response = bulkService.bulkCreateFromExcel(file);
        return ResponseEntity.status(getHttpStatus(response)).body(response);
    }

    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.ms-excel"));
    }

    private HttpStatus getHttpStatus(BulkUploadResponse response) {
        if (response.getSuccessfulCount() == 0) {
            return HttpStatus.BAD_REQUEST;
        } else if (response.getFailedCount() > 0) {
            return HttpStatus.MULTI_STATUS;
        } else {
            return HttpStatus.CREATED;
        }
    }
}
