package returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import returnstrackingsystem.domain.ReturnDefinition;

public interface ReturnDefinitionService {
    ReturnDefinition createReport(ReturnDefinitionRequest reportDTO);
    ReturnDefinition getReportById(Long id);
    ReturnDefinition updateReport(Long id, ReturnDefinitionRequest reportDTO);
    Page<ReturnDefinition> getAllReports(String search, Frequency frequency,
                                         String departmentName, Boolean isDeleted,
                                         Pageable pageable);
    boolean deleteReport(Long id);
}
