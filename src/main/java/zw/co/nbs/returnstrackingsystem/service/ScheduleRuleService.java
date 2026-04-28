package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.dtos.request.ScheduleRuleRequest;
import zw.co.nbs.returnstrackingsystem.domain.ScheduleRule;
import zw.co.nbs.returnstrackingsystem.dtos.request.ScheduleRuleUpdateRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;


public interface ScheduleRuleService {
    ScheduleRule createScheduleRule(ScheduleRuleRequest scheduleRuleRequest);
    ScheduleRule updateScheduleRule(Long id, ScheduleRuleUpdateRequest updateRequest);
    Page<ScheduleRule> getAllScheduleRules(String departmentName, Pageable pageable);
    ScheduleRule getScheduleRule(Long id);
    BulkUploadResponse bulkScheduleRules(MultipartFile file);
}
