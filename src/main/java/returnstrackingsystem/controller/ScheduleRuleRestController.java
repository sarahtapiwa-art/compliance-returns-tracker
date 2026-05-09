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
import returnstrackingsystem.dtos.request.ScheduleRuleRequest;
import returnstrackingsystem.dtos.request.ScheduleRuleUpdateRequest;
import returnstrackingsystem.dtos.response.BulkUploadError;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.dtos.response.PagedResponse;
import returnstrackingsystem.dtos.response.ScheduleRuleResponse;
import returnstrackingsystem.convertor.ScheduleRuleObjectMapper;
import returnstrackingsystem.service.ScheduleRuleService;
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
@RequestMapping("api/v1/schedule-rule")
@RequiredArgsConstructor
@Tag(name = "Schedule Rule API", description = "CRUD operations for schedule rules")
public class ScheduleRuleRestController {

    private final ScheduleRuleService scheduleRuleService;
    private final ScheduleRuleObjectMapper scheduleRuleObjectMapper;

    @Operation(summary = "Create a new schedule rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schedule rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ScheduleRuleResponse> createScheduleRule(
            @Parameter(description = "Schedule rule request payload", required = true)
            @Valid @RequestBody ScheduleRuleRequest scheduleRuleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleRuleObjectMapper.toScheduleRuleResponse(
                        scheduleRuleService.createScheduleRule(scheduleRuleRequest)
                ));
    }

    @Operation(summary = "Update schedule rule by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{scheduleRuleId}")
    public ResponseEntity<ScheduleRuleResponse> updateScheduleRule(
            @Parameter(description = "ID of the schedule rule to update", required = true)
            @PathVariable("scheduleRuleId") Long scheduleRuleId,
            @Parameter(description = "Schedule rule update request payload", required = true)
            @Valid @RequestBody ScheduleRuleUpdateRequest updateRequest
    ){
        return ResponseEntity.ok(
                scheduleRuleObjectMapper.toScheduleRuleResponse(
                        scheduleRuleService.updateScheduleRule(scheduleRuleId, updateRequest)
                )
        );
    }

    @Operation(summary = "Get all schedule rules with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule rules retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ScheduleRuleResponse>> getScheduleRules(
            @Parameter(description = "Department name ", example = "ICT")
            @RequestParam(required = false) String departmentName,
            @Parameter(description = "Pagination information")
            @PageableDefault(direction = DESC,sort="id") Pageable pageable) {
        var page = scheduleRuleService.getAllScheduleRules(departmentName,pageable)
                .map(scheduleRuleObjectMapper::toScheduleRuleResponse);

        return ResponseEntity.ok(
                PagedResponse.<ScheduleRuleResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }

    @Operation(summary = "Get schedule rule by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule rule retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Schedule rule not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleRuleResponse> getScheduleRuleById(
            @Parameter(description = "ID of the schedule rule to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(scheduleRuleObjectMapper.toScheduleRuleResponse(
                scheduleRuleService.getScheduleRule(id)
        ));
    }

    @Operation(summary = "Bulk upload schedule rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk upload completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUploadScheduleRules(
            @RequestParam("file") MultipartFile file) {

        log.info("Received bulk upload request for schedule rules. File: {}, Size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        ResponseEntity<BulkUploadResponse> errorResponse = getBulkUploadResponseResponseEntity(isExcelFile(file), file);
        if (errorResponse != null) return errorResponse;

        try {
            BulkUploadResponse response = scheduleRuleService.bulkScheduleRules(file);
            log.info("Schedule rules bulk upload completed. Total: {}, Success: {}, Failed: {}",
                    response.getTotalProcessed(), response.getSuccessfulCount(), response.getFailedCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing schedule rules bulk upload", e);
            return ResponseEntity.internalServerError()
                    .body(BulkUploadResponse.builder()
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
