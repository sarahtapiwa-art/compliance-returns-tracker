package returnstrackingsystem.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import returnstrackingsystem.domain.Department;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d FROM Department d WHERE (:deleted IS NULL OR d.deleted = :deleted)")
    Page<Department> findByDeletedFilter(@Param("deleted") Boolean deleted, Pageable pageable);

    Optional<Department> findByDepartmentName(String departmentName);

    Optional<Department> findByDepartmentNameIgnoreCase(@NotBlank(message = "Department name is mandatory") String departmentName);
}