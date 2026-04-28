package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import zw.co.nbs.returnstrackingsystem.domain.ResponsiblePerson;
import zw.co.nbs.returnstrackingsystem.dtos.request.DepartmentRequest;
import zw.co.nbs.returnstrackingsystem.domain.Department;
import zw.co.nbs.returnstrackingsystem.dtos.response.BulkUploadResponse;

public interface DepartmentService {
    Department createDepartment(DepartmentRequest departmentDTO);

    Page<Department> getAllDepartments(Boolean deleted, Pageable pageable);
    Department getDepartmentById(Long id);
    Department updateDepartment(Long id, DepartmentRequest departmentDTO);
    boolean deleteDepartment(Long id);
    BulkUploadResponse bulkDepartments(MultipartFile file);
}
