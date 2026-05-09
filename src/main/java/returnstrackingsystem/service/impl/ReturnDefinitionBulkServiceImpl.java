package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import returnstrackingsystem.dtos.response.BulkUploadError;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.repository.DepartmentRepository;
import returnstrackingsystem.service.ReturnDefinitionBulkService;
import returnstrackingsystem.service.ReturnDefinitionService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 11:03
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnDefinitionBulkServiceImpl implements ReturnDefinitionBulkService {

    private final ReturnDefinitionService returnDefinitionService;
    private final DepartmentRepository departmentRepository;

    @Override
    public BulkUploadResponse bulkCreateReports(List<ReturnDefinitionRequest> reportRequests) {
        log.info("Starting bulk upload for {} reports", reportRequests.size());

        List<BulkUploadError> errors = new ArrayList<>();
        List<Long> successfulReportIds = new ArrayList<>();

        for (int i = 0; i < reportRequests.size(); i++) {
            ReturnDefinitionRequest reportRequest = reportRequests.get(i);

            try {
                validateReportRequest(reportRequest, i);
                ReturnDefinition createdReport = returnDefinitionService.createReport(reportRequest);
                successfulReportIds.add(createdReport.getId());
                log.debug("Successfully created report at index {} with ID: {}", i, createdReport.getId());

            } catch (Exception e) {
                log.error("Failed to create report at index {}: {}", i, e.getMessage());
                errors.add(createBulkUploadError(i, reportRequest, e));
            }
        }

        return buildBulkUploadResponse(reportRequests.size(), successfulReportIds.size(), errors, successfulReportIds);
    }

    @Override
    public BulkUploadResponse bulkCreateFromExcel(MultipartFile file) {
        try {
            List<ReturnDefinitionRequest> reportRequests = parseExcelFile(file);
            return bulkCreateReports(reportRequests);
        } catch (IOException e) {
            log.error("Error processing Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }
    }

    private List<ReturnDefinitionRequest> parseExcelFile(MultipartFile file) throws IOException {
        List<ReturnDefinitionRequest> reportRequests = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    ReturnDefinitionRequest reportRequest = mapRowToReturnDefinitionRequest(row);
                    reportRequests.add(reportRequest);
                } catch (Exception e) {
                    log.warn("Skipping invalid row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        return reportRequests;
    }

    private ReturnDefinitionRequest mapRowToReturnDefinitionRequest(Row row) {
        return ReturnDefinitionRequest.builder()
                .title(getCellStringValue(row.getCell(0)))
                .regulatoryBody(getCellStringValue(row.getCell(1)))
                .regulatoryEmail(getCellStringValue(row.getCell(2)))
                .frequency(parseFrequency(getCellStringValue(row.getCell(3))))
                .submissionDeadline(parseDeadline(getCellStringValue(row.getCell(4)))
                        .toLocalDateTime())
                .responsibleDepartmentId(parseDepartmentId(row.getCell(5)))
                .description(getCellStringValue(row.getCell(6)))
                .build();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toInstant()
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } else {
                    return valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private void validateReportRequest(ReturnDefinitionRequest reportRequest, int index) {
        if (!departmentRepository.existsById(reportRequest.getResponsibleDepartmentId())) {
            throw new RecordNotFoundException(
                    format("Department with id: %d not found for report at index %d",
                            reportRequest.getResponsibleDepartmentId(), index));
        }

        if (reportRequest.getSubmissionDeadline().isBefore(OffsetDateTime.now()
                .toLocalDateTime())) {
            throw new IllegalArgumentException(
                    format("Submission deadline must be in the future for report at index %d", index));
        }

        if (reportRequest.getFrequency() == null) {
            throw new IllegalArgumentException(
                    format("Frequency is required for report at index %d", index));
        }
    }

    private BulkUploadError createBulkUploadError(int index, ReturnDefinitionRequest reportRequest, Exception e) {
        return BulkUploadError.builder()
                .index(index)
                .title(reportRequest.getTitle())
                .error(e.getMessage())
                .field(determineErrorField(e))
                .build();
    }

    private String determineErrorField(Exception e) {
        if (e instanceof RecordNotFoundException && e.getMessage()
                .contains("Department")) {
            return "responsibleDepartmentId";
        } else if (e.getMessage().contains("title")) {
            return "title";
        } else if (e.getMessage().contains("frequency")) {
            return "frequency";
        } else if (e.getMessage().contains("deadline")) {
            return "submissionDeadline";
        }
        return "general";
    }

    private BulkUploadResponse buildBulkUploadResponse(int total, int successful,
            List<BulkUploadError> errors,
            List<Long> successfulIds) {
        return BulkUploadResponse.builder()
                .totalProcessed(total)
                .successfulCount(successful)
                .failedCount(errors.size())
                .errors(errors)
                .successfulIds(successfulIds)
                .build();
    }

    private Frequency parseFrequency(String frequencyStr) {
        if (frequencyStr == null)
            return null;
        try {
            return Frequency.valueOf(frequencyStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid frequency: " + frequencyStr);
        }
    }

    private OffsetDateTime parseDeadline(String deadlineStr) {
        if (deadlineStr == null)
            return null;
        try {
            return OffsetDateTime.parse(deadlineStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid deadline format: " + deadlineStr);
        }
    }

    private Long parseDepartmentId(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid department ID: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Department ID must be a number");
        }
    }

}
