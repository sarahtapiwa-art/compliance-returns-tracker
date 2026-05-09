package returnstrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.dtos.request.DepartmentRequest;
import returnstrackingsystem.dtos.response.BulkUploadError;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.dtos.response.DepartmentResponse;
import returnstrackingsystem.convertor.DepartmentObjectMapper;
import returnstrackingsystem.dtos.response.PagedResponse;
import returnstrackingsystem.service.impl.DepartmentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static returnstrackingsystem.util.AppUtil.getBulkUploadResponseResponseEntity;
import static returnstrackingsystem.util.AppUtil.isExcelFile;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department API", description = "CRUD operations for departments")
public class DepartmentController {

    private final DepartmentServiceImpl departmentService;
    private final DepartmentObjectMapper departmentObjectMapper;

    @Operation(summary = "Create a new department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Parameter(description = "Department request payload", required = true)
            @Valid @RequestBody DepartmentRequest departmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentObjectMapper
                        .toDepartmentResponse(departmentService.createDepartment(departmentDTO)));
    }

    @Operation(summary = "Get all departments with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of departments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<DepartmentResponse>> getAllDepartments(
            @RequestParam(required = false) Boolean deleted,
            @Parameter(description = "Pagination information")
            @PageableDefault(direction = DESC,sort="id") Pageable pageable) {
        var page = departmentService.getAllDepartments(deleted, pageable)
                .map(departmentObjectMapper::toDepartmentResponse);

        return ResponseEntity.ok(
                PagedResponse.<DepartmentResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }

    @Operation(summary = "Get department by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @Parameter(description = "ID of the department to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(departmentObjectMapper
                .toDepartmentResponse(departmentService.getDepartmentById(id)));
    }

    @Operation(summary = "Update department by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @Parameter(description = "ID of the department to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated department payload", required = true)
            @Valid @RequestBody DepartmentRequest departmentDTO) {
        return ResponseEntity.ok(departmentObjectMapper
                .toDepartmentResponse(departmentService.updateDepartment(id, departmentDTO)));
    }

    @Operation(summary = "Delete department by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department to delete", required = true)
            @PathVariable Long id) {
        return departmentService.deleteDepartment(id)
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Bulk upload departments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk upload completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUploadDepartments(
            @RequestParam("file") MultipartFile file) {

        log.info("Received bulk upload request for departments. File: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        ResponseEntity<BulkUploadResponse> errorResponse = getBulkUploadResponseResponseEntity(isExcelFile(file), file);
        if (errorResponse != null) return errorResponse;

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body( BulkUploadResponse.builder()
                            .totalProcessed(0)
                            .successfulCount(0)
                            .failedCount(1)
                            .errors(List.of(BulkUploadError.builder()
                                    .index(0)
                                    .title("File Validation")
                                    .error("File size too large. Maximum size is 10MB")
                                    .field("file")
                                    .build()))
                            .successfulIds(Collections.emptyList())
                            .build()
                    );
        }

        try {
            BulkUploadResponse response = departmentService.bulkDepartments(file);
            log.info("Bulk upload completed. Total: {}, Success: {}, Failed: {}",
                    response.getTotalProcessed(), response.getSuccessfulCount(), response.getFailedCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing bulk upload", e);
            return ResponseEntity.internalServerError()
                    .body( BulkUploadResponse.builder()
                            .totalProcessed(0)
                            .successfulCount(0)
                            .failedCount(1)
                            .errors(List.of(BulkUploadError.builder()
                                    .index(0)
                                    .title("System Error")
                                    .error("Error processing file: " + e.getMessage())
                                    .field("system")
                                    .build()))
                            .successfulIds(Collections.emptyList())
                            .build()
                    );
        }
    }
}
