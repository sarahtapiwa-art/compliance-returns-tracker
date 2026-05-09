package returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import returnstrackingsystem.dtos.request.DepartmentRequest;
import returnstrackingsystem.domain.Department;
import returnstrackingsystem.dtos.response.BulkUploadResponse;

public interface DepartmentService {
    Department createDepartment(DepartmentRequest departmentDTO);

    Page<Department> getAllDepartments(Boolean deleted, Pageable pageable);
    Department getDepartmentById(Long id);
    Department updateDepartment(Long id, DepartmentRequest departmentDTO);
    boolean deleteDepartment(Long id);
    BulkUploadResponse bulkDepartments(MultipartFile file);
}
