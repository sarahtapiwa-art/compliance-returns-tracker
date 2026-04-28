package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zw.co.nbs.returnstrackingsystem.domain.enums.Frequency;
import zw.co.nbs.returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;

public interface ReturnDefinitionService {
    ReturnDefinition createReport(ReturnDefinitionRequest reportDTO);
    ReturnDefinition getReportById(Long id);
    ReturnDefinition updateReport(Long id, ReturnDefinitionRequest reportDTO);
    Page<ReturnDefinition> getAllReports(String search, Frequency frequency,
                                         String departmentName, Boolean isDeleted,
                                         Pageable pageable);
    boolean deleteReport(Long id);
}
