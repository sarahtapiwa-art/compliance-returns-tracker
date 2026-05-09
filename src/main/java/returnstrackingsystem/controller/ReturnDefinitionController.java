package returnstrackingsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import returnstrackingsystem.dtos.response.PagedResponse;
import returnstrackingsystem.dtos.response.ReturnDefinitionResponse;
import returnstrackingsystem.convertor.ReturnDefinitionObjectMapper;
import returnstrackingsystem.service.impl.ReturnDefinitionServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/v1/return-definition")
@RequiredArgsConstructor
@Tag(name = "Return Definition API", description = "CRUD operations for regulatory reports")
public class ReturnDefinitionController {

    private final ReturnDefinitionServiceImpl regulatoryReportService;
    private final ReturnDefinitionObjectMapper regulatoryReportObjectMapper;

    @Operation(summary = "Create a new regulatory report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ReturnDefinitionResponse> createReport(
            @Parameter(description = "Report request payload", required = true)
            @Valid @RequestBody ReturnDefinitionRequest reportDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(regulatoryReportObjectMapper
                        .toReturnDefinitionResponse(regulatoryReportService.createReport(reportDTO)));
    }

    @Operation(summary = "Get all regulatory reports with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ReturnDefinitionResponse>> getAllReports(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Frequency frequency,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault(size = 50, direction = DESC, sort="id") Pageable pageable) {
        var page =  regulatoryReportService.getAllReports(search, frequency,
                        departmentName, isDeleted, pageable)
                .map(regulatoryReportObjectMapper::toReturnDefinitionResponse);

        return ResponseEntity.ok(
                PagedResponse.<ReturnDefinitionResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }

    @Operation(summary = "Get regulatory report by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReturnDefinitionResponse> getReportById(
            @Parameter(description = "ID of the report to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(regulatoryReportObjectMapper
                .toReturnDefinitionResponse(regulatoryReportService.getReportById(id)));
    }

    @Operation(summary = "Update regulatory report by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReturnDefinitionResponse> updateReport(
            @Parameter(description = "ID of the report to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated report payload", required = true)
            @Valid @RequestBody ReturnDefinitionRequest reportDTO) {
        return ResponseEntity.ok(regulatoryReportObjectMapper
                .toReturnDefinitionResponse(regulatoryReportService.updateReport(id, reportDTO)));
    }

    @Operation(summary = "Delete regulatory report by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(
            @Parameter(description = "ID of the report to delete", required = true)
            @PathVariable Long id) {
        boolean deleted = regulatoryReportService.deleteReport(id);

        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "Report with ID " + id + " was successfully deleted.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Report with ID " + id + " not found or could not be deleted.");
            return ResponseEntity.status(NOT_FOUND).body(response);
        }
    }
}