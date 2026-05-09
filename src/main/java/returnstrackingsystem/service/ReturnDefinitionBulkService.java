package returnstrackingsystem.service;

import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.dtos.request.ReturnDefinitionRequest;
import returnstrackingsystem.dtos.response.BulkUploadResponse;

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
