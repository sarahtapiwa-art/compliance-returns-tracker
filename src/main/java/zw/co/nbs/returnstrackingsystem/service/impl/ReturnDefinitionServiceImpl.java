package zw.co.nbs.returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import zw.co.nbs.returnstrackingsystem.domain.Department;
import zw.co.nbs.returnstrackingsystem.domain.ResponsiblePerson;
import zw.co.nbs.returnstrackingsystem.domain.enums.Frequency;
import zw.co.nbs.returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import zw.co.nbs.returnstrackingsystem.exception.RecordNotFoundException;
import zw.co.nbs.returnstrackingsystem.convertor.ReturnDefinitionObjectMapper;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;
import zw.co.nbs.returnstrackingsystem.repository.DepartmentRepository;
import zw.co.nbs.returnstrackingsystem.repository.ReturnDefinitionRepository;
import zw.co.nbs.returnstrackingsystem.service.ReturnDefinitionService;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static zw.co.nbs.returnstrackingsystem.util.TimeUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnDefinitionServiceImpl implements ReturnDefinitionService {

    private final ReturnDefinitionRepository returnDefinitionRepository;
    private final ReturnDefinitionObjectMapper regulatoryReportObjectMapper;
    private final DepartmentRepository departmentRepository;

    @Override
    public ReturnDefinition createReport(ReturnDefinitionRequest reportDTO) {
        requireNonNull(reportDTO, "ReturnDefinition Report DTO cannot be null");

        Department department = departmentRepository
                .findById(reportDTO.getResponsibleDepartmentId())
                .filter(dept -> !dept.isDeleted())
                .orElseThrow(() -> new RecordNotFoundException(
                        format("Active department with id: %d not found",
                                reportDTO.getResponsibleDepartmentId())
                ));

        log.info("Creating new ReturnDefinition report: {} for department: {}",
                reportDTO.getTitle(), department.getDepartmentName());

        var report = ReturnDefinition.builder()
                .regulatoryBody(reportDTO.getRegulatoryBody())
                .regulatoryEmail(reportDTO.getRegulatoryEmail())
                .title(reportDTO.getTitle())
                .description(reportDTO.getDescription())
                .submissionDeadline(
                        convertToHarareOffsetDateTime(
                                reportDTO.getSubmissionDeadline()
                        )
                ).frequency(reportDTO.getFrequency())
                .build();
        report.setDepartment(department);
        
        var newPerson = ResponsiblePerson.builder()
                .name(reportDTO.getResponsiblePerson().getName() != null ?
                        reportDTO.getResponsiblePerson().getName().trim() : null)
                .surname(reportDTO.getResponsiblePerson().getSurname() != null ?
                        reportDTO.getResponsiblePerson().getSurname().trim() : null)
                .email(reportDTO.getResponsiblePerson().getEmail() != null ?
                        reportDTO.getResponsiblePerson().getEmail().trim().toLowerCase() : null)
                .build();

        report.setResponsiblePerson(newPerson);
        log.info("Saving ReturnDefinition report");
        return returnDefinitionRepository.save(report);
    }

    @Override
    public Page<ReturnDefinition> getAllReports(String search, Frequency frequency,
                                                String departmentName, Boolean isDeleted,
                                                Pageable pageable) {
        log.info("Getting all ReturnDefinition reports");
        return returnDefinitionRepository.findAllWithFilters(search, frequency,
                departmentName, isDeleted, pageable);
    }


    @Override
    public ReturnDefinition getReportById(Long id) {
        return returnDefinitionRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException(
                        format("Regulatory Report with id: %d, not found", id)
                )
        );
    }

    @Override
    public ReturnDefinition updateReport(Long id, ReturnDefinitionRequest reportDTO) {
        requireNonNull(reportDTO, "Regulatory Report DTO cannot be null");
        requireNonNull(id, "Regulatory Report id cannot be null");

        log.info("Updating regulatory report with id: {}", id);
        ReturnDefinition report = getReportById(id);
        report.setRegulatoryBody(reportDTO.getRegulatoryBody());
        report.setRegulatoryEmail(reportDTO.getRegulatoryEmail());
        report.setTitle(reportDTO.getTitle());
        report.setDescription(reportDTO.getDescription());
        report.setSubmissionDeadline(convertToHarareOffsetDateTime(reportDTO.getSubmissionDeadline()));
        report.setFrequency(reportDTO.getFrequency());

        log.info("Updating regulatory report: {}", report);
        ReturnDefinition savedReport = returnDefinitionRepository.save(report);

        log.info("Saved updated regulatory report: {}", savedReport);
        return savedReport;

    }

    @Override
    public boolean deleteReport(Long id) {
        requireNonNull(id, "ReturnDefinition id cannot be null");
        log.info("Deleting ReturnDefinition with id: {}", id);
        return returnDefinitionRepository.findById(id)
                .map(returnDefinition -> {
                    if (returnDefinition.isDeleted()) {
                        log.warn("Attempted to delete ReturnDefinition with ID: {} " +
                                "which is already deleted", id);
                        return false;
                    }
                    returnDefinition.setDeleted(true);
                    returnDefinitionRepository.save(returnDefinition);
                    log.info("ReturnDefinition with ID: {} marked as deleted", id);
                    return true;
                })
                .orElse(false);

    }
}