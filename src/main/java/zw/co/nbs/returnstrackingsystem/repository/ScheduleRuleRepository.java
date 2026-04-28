package zw.co.nbs.returnstrackingsystem.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;
import zw.co.nbs.returnstrackingsystem.domain.ScheduleRule;

import java.util.Optional;

public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule, Long> {
    Optional<ScheduleRule> findByReturnDefinition(ReturnDefinition returnDefinition);
    boolean existsByReturnDefinition(ReturnDefinition returnDefinition);

    Optional<ScheduleRule> findByReturnDefinitionId(@NotNull(message = "ReturnDefinition id is required") Long returnDefinitionId);
    @Query("SELECT sr FROM ScheduleRule sr " +
            "JOIN FETCH sr.returnDefinition rd " +
            "JOIN FETCH rd.department d " +
            "WHERE (:departmentName IS NULL OR rd.department.departmentName = :departmentName) " +
            "AND rd.deleted = false")
    Page<ScheduleRule> findAllWithFilters(@Param("departmentName") String departmentName,
                                          Pageable pageable);
}
