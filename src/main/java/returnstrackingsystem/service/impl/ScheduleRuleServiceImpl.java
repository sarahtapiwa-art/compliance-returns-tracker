package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.dtos.request.ScheduleRuleRequest;
import returnstrackingsystem.domain.ScheduleRule;
import returnstrackingsystem.dtos.request.ScheduleRuleUpdateRequest;
import returnstrackingsystem.dtos.response.BulkUploadError;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.exception.ValidationException;
import returnstrackingsystem.repository.ReturnDefinitionRepository;
import returnstrackingsystem.repository.ScheduleRuleRepository;
import returnstrackingsystem.service.ScheduleRuleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.poi.ss.usermodel.CellType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleRuleServiceImpl implements ScheduleRuleService {

    private final ScheduleRuleRepository scheduleRuleRepository;
    private final ReturnDefinitionRepository returnDefinitionRepository;

    @Override
    public ScheduleRule createScheduleRule(ScheduleRuleRequest scheduleRuleRequest) {
        requireNonNull(scheduleRuleRequest, "ScheduleRule Request cannot be null");

        var returnDefinition = returnDefinitionRepository.findById(scheduleRuleRequest.getReturnDefinitionId())
                .orElseThrow(() -> new RecordNotFoundException(
                        format("Return definition with id %s not found",
                                scheduleRuleRequest.getReturnDefinitionId())
                ));
        log.debug("Creating schedule rule for return definition report: {}", returnDefinition.getTitle());
        if (scheduleRuleRepository.existsByReturnDefinition(returnDefinition)) {
            throw new IllegalArgumentException(
                    format("Schedule rule already exists for return definition: %s",
                            returnDefinition.getTitle())
            );
        }
        ScheduleRule scheduleRule = ScheduleRule.builder()
                .escalateAfterHours(scheduleRuleRequest.getEscalateAfterHours())
                .remindDaysBefore(scheduleRuleRequest.getRemindDaysBefore())
                .returnDefinition(returnDefinition)
                .build();
        log.debug("Saving schedule rule: {}", scheduleRule);
        return scheduleRuleRepository.save(scheduleRule);
    }

    @Override
    public ScheduleRule updateScheduleRule(Long id, ScheduleRuleUpdateRequest updateRequest) {
        requireNonNull(id, "ScheduleRule Id cannot be null");
        requireNonNull(updateRequest, "ScheduleRule Update Request cannot be null");

        log.debug("Updating schedule rule with id: {}", id);
        var scheduleRule = getScheduleRule(id);
        var returnDefinition = returnDefinitionRepository.findById(updateRequest.getReturnDefinitionId())
                        .orElseThrow(() -> new RecordNotFoundException(
                           format("ReturnDefinition with id %s not found", updateRequest.getReturnDefinitionId())
                        ));
        scheduleRule.setReturnDefinition(returnDefinition);
        scheduleRule.setEscalateAfterHours(updateRequest.getEscalateAfterHours());
        scheduleRule.setRemindDaysBefore(updateRequest.getRemindDaysBefore());
        return scheduleRuleRepository.save(scheduleRule);
    }

    @Override
    public Page<ScheduleRule> getAllScheduleRules(String departmentName, Pageable pageable) {
        log.debug("Retrieving all schedule rules");
        return scheduleRuleRepository.findAllWithFilters(departmentName,pageable);
    }

    @Override
    public ScheduleRule getScheduleRule(Long id) {
        requireNonNull(id, "ScheduleRule Id cannot be null");

        log.debug("Retrieving schedule rule with id: {}", id);
        return scheduleRuleRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(
                        format("Schedule rule with id %s not found", id)
                ));
    }

    @Override
    public BulkUploadResponse bulkScheduleRules(MultipartFile file) {
        List<BulkUploadError> errors = new ArrayList<>();
        List<Long> successfulReportIds = new ArrayList<>();
        int totalProcessed = 0;
        int successfulCount = 0;

        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                totalProcessed++;
                Long returnDefinitionId = getLongCellValue(row.getCell(0));

                try {
                    ScheduleRuleRequest scheduleRuleRequest = mapRowToScheduleRuleRequest(row);
                    validateScheduleRuleRequest(scheduleRuleRequest);

                    // Save schedule rule
                    ScheduleRule scheduleRule = saveScheduleRule(scheduleRuleRequest);
                    successfulReportIds.add(scheduleRule.getId());
                    successfulCount++;

                } catch (Exception e) {
                    errors.add(createBulkUploadError(i, returnDefinitionId != null ? returnDefinitionId.toString() : "N/A", e));
                }
            }

            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        }

        return BulkUploadResponse.builder()
                .totalProcessed(totalProcessed)
                .successfulCount(successfulCount)
                .failedCount(errors.size())
                .errors(errors)
                .successfulIds(successfulReportIds)
                .build();
    }

    private ScheduleRuleRequest mapRowToScheduleRuleRequest(Row row) {
        return ScheduleRuleRequest.builder()
                .returnDefinitionId(getLongCellValue(row.getCell(0))) // Column A
                .remindDaysBefore(getIntegerCellValue(row.getCell(1))) // Column B
                .escalateAfterHours(getIntegerCellValue(row.getCell(2))) // Column C
                .build();
    }

    private Long getLongCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String stringValue = cell.getStringCellValue().trim();
                    return stringValue.isEmpty() ? null : Long.parseLong(stringValue);
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case FORMULA:
                    return switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC -> (long) cell.getNumericCellValue();
                        case STRING -> {
                            String formulaValue = cell.getStringCellValue().trim();
                            yield formulaValue.isEmpty() ? null : Long.parseLong(formulaValue);
                        }
                        default -> null;
                    };
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format for Return Definition ID", "returnDefinitionId");
        }
    }

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    String stringValue = cell.getStringCellValue().trim();
                    return stringValue.isEmpty() ? null : Integer.parseInt(stringValue);
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case FORMULA:
                    return switch (cell.getCachedFormulaResultType()) {
                        case NUMERIC -> (int) cell.getNumericCellValue();
                        case STRING -> {
                            String formulaValue = cell.getStringCellValue().trim();
                            yield formulaValue.isEmpty() ? null : Integer.parseInt(formulaValue);
                        }
                        default -> null;
                    };
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format", "numericField");
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }

    private void validateScheduleRuleRequest(ScheduleRuleRequest request) {
        if (request.getReturnDefinitionId() == null) {
            throw new ValidationException("Return Definition ID is mandatory", "returnDefinitionId");
        }

        Optional<ReturnDefinition> returnDefinition = returnDefinitionRepository.findById(request.getReturnDefinitionId());
        if (returnDefinition.isEmpty()) {
            throw new ValidationException("Return Definition with ID " + request.getReturnDefinitionId() + " not found", "returnDefinitionId");
        }

        if (request.getRemindDaysBefore() != null && request.getRemindDaysBefore() < 0) {
            throw new ValidationException("Remind Days Before cannot be negative", "remindDaysBefore");
        }

        if (request.getEscalateAfterHours() != null && request.getEscalateAfterHours() < 0) {
            throw new ValidationException("Escalate After Hours cannot be negative", "escalateAfterHours");
        }
    }

    private boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return true; // Optional field
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private ScheduleRule saveScheduleRule(ScheduleRuleRequest request) {
        requireNonNull(request, "ScheduleRuleRequest cannot be null");

        ReturnDefinition returnDefinition = returnDefinitionRepository.findById(request.getReturnDefinitionId())
                .orElseThrow(() -> new ValidationException("Return Definition not found", "returnDefinitionId"));

        Optional<ScheduleRule> existingScheduleRule = scheduleRuleRepository.findByReturnDefinitionId(request.getReturnDefinitionId());

        ScheduleRule scheduleRule;
        if (existingScheduleRule.isPresent()) {
            log.info("Updating existing schedule rule for return definition: {}",
                    returnDefinition.getTitle());
            scheduleRule = existingScheduleRule.get();
            scheduleRule.setRemindDaysBefore(request.getRemindDaysBefore());
            scheduleRule.setEscalateAfterHours(request.getEscalateAfterHours());

        } else {
            log.info("Creating new schedule rule for return definition: {}",
                    returnDefinition.getTitle());
            scheduleRule = ScheduleRule.builder()
                    .returnDefinition(returnDefinition)
                    .remindDaysBefore(request.getRemindDaysBefore())
                    .escalateAfterHours(request.getEscalateAfterHours())
                    .build();
        }

        return scheduleRuleRepository.save(scheduleRule);
    }

    private BulkUploadError createBulkUploadError(int rowIndex, String returnDefinitionId, Exception e) {
        String errorMessage = e.getMessage();
        String field = null;

        if (e instanceof ValidationException ve) {
            field = ve.getField();
        }

        return BulkUploadError.builder()
                .index(rowIndex)
                .title("ReturnDefID: " + returnDefinitionId)
                .error(errorMessage)
                .field(field)
                .build();
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;

        return IntStream.range(0, row.getLastCellNum())
                .mapToObj(row::getCell)
                .filter(cell -> cell != null && cell.getCellType() != BLANK)
                .map(this::getCellValue)
                .noneMatch(StringUtils::isNotBlank);
    }
}
