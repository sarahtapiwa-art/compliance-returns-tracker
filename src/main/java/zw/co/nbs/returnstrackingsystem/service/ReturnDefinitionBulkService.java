package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.dtos.request.DepartmentRequest;
import zw.co.nbs.returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;

import java.util.List;

/**
 * createdBy romeo
 * createdDate 17/10/2025
 * createdTime 10:58
 * projectName compliance-returns-tracker
 **/

public interface ReturnDefinitionBulkService {
    BulkUploadResponse bulkCreateReports(List<ReturnDefinitionRequest> reportRequests);
    BulkUploadResponse bulkCreateFromExcel(MultipartFile file);
}
