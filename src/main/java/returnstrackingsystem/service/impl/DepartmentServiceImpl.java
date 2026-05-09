package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.dtos.request.DepartmentRequest;
import returnstrackingsystem.dtos.response.BulkUploadError;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.convertor.DepartmentObjectMapper;
import returnstrackingsystem.domain.Department;
import returnstrackingsystem.exception.ValidationException;
import returnstrackingsystem.repository.DepartmentRepository;
import returnstrackingsystem.service.DepartmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentObjectMapper departmentObjectMapper;

    @Override
    public Department createDepartment(DepartmentRequest departmentDTO) {
        requireNonNull(departmentDTO, "Department DTO cannot be null");

        log.info("Creating new department");
        var newDepartment = departmentObjectMapper.toDepartment(departmentDTO);

        log.info("Saving new department");
        return departmentRepository.save(newDepartment);
    }

    @Override
    public Page<Department> getAllDepartments(Boolean deleted, Pageable pageable) {
        log.info("Getting all departments");
        return departmentRepository.findByDeletedFilter(deleted, pageable);
    }

    @Override
    public Department getDepartmentById(Long id) {
        requireNonNull(id, "Department id cannot be null");

        log.info("Getting department with id: {}", id);
        return departmentRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException(
                        format("Department with id: %d, not found", id)));
    }

    @Override
    public Department updateDepartment(Long id, DepartmentRequest departmentDTO) {
        requireNonNull(departmentDTO, "Department DTO cannot be null");
        requireNonNull(id, "Department id cannot be null");

        log.info("Updating department with id: {}", id);
        var department = getDepartmentById(id);
        departmentObjectMapper.updateDepartment(departmentDTO, department);
        log.info("Saving updated department");
        return departmentRepository.save(department);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    if (department.isDeleted()) {
                        log.warn("Attempted to delete Department with ID: {}" +
                                " which is already deleted", id);
                        return false;
                    }
                    department.setDeleted(true);
                    departmentRepository.save(department);
                    log.info("Department with ID: {} marked as deleted", id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public BulkUploadResponse bulkDepartments(MultipartFile file) {
        List<BulkUploadError> errors = new ArrayList<>();
        List<Long> successfulReportIds = new ArrayList<>();
        int totalProcessed = 0;
        int successfulCount = 0;

        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row))
                    continue;

                totalProcessed++;
                String departmentName = getCellValue(row.getCell(0));

                try {
                    DepartmentRequest departmentRequest = mapRowToDepartmentRequest(row);
                    validateDepartmentRequest(departmentRequest);

                    // Save department
                    Department department = saveDepartment(departmentRequest);
                    successfulReportIds.add(department.getId());
                    successfulCount++;

                } catch (Exception e) {
                    errors.add(createBulkUploadError(i, departmentName, e));
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

    private DepartmentRequest mapRowToDepartmentRequest(Row row) {
        return DepartmentRequest.builder()
                .departmentName(getCellValue(row.getCell(0))) // Column A
                .escalationEmail(getCellValue(row.getCell(1))) // Column B
                .headOfDepartmentEmail(getCellValue(row.getCell(2))) // Column C
                .build();
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
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void validateDepartmentRequest(DepartmentRequest request) {
        List<String> validationErrors = new ArrayList<>();

        if (StringUtils.isBlank(request.getDepartmentName())) {
            validationErrors.add("Department name is mandatory");
        }

        if (StringUtils.isNotBlank(request.getEscalationEmail()) &&
                !isValidEmail(request.getEscalationEmail())) {
            validationErrors.add("Escalation email is invalid");
        }

        if (StringUtils.isNotBlank(request.getHeadOfDepartmentEmail()) &&
                !isValidEmail(request.getHeadOfDepartmentEmail())) {
            validationErrors.add("Head of department email is invalid");
        }

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", validationErrors));
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex)
                .matcher(email)
                .matches();
    }

    private Department saveDepartment(DepartmentRequest request) {
        Optional<Department> existingDepartment = departmentRepository
                .findByDepartmentNameIgnoreCase(request.getDepartmentName());

        Department department;
        if (existingDepartment.isPresent()) {
            // Update existing department
            department = existingDepartment.get();
            department.setEscalationEmail(request.getEscalationEmail());
            department.setHeadOfDepartmentEmail(request.getHeadOfDepartmentEmail());
        } else {
            department = Department.builder()
                    .departmentName(request.getDepartmentName())
                    .escalationEmail(request.getEscalationEmail())
                    .headOfDepartmentEmail(request.getHeadOfDepartmentEmail())
                    .build();
        }

        return departmentRepository.save(department);
    }

    private BulkUploadError createBulkUploadError(int rowIndex, String departmentName, Exception e) {
        String errorMessage = e.getMessage();
        String field = null;

        if (e instanceof ValidationException ve) {
            field = ve.getField();
        }

        if (field != null && errorMessage != null && errorMessage.contains(";")) {
            errorMessage = errorMessage.split(";")[0].trim();
        }

        return BulkUploadError.builder()
                .index(rowIndex)
                .title(departmentName)
                .error(errorMessage)
                .field(field)
                .build();
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell);
                if (StringUtils.isNotBlank(value)) {
                    return false;
                }
            }
        }
        return true;
    }
}