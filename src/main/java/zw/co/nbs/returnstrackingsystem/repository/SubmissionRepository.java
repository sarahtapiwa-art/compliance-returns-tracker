package zw.co.nbs.returnstrackingsystem.repository;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zw.co.nbs.returnstrackingsystem.domain.ReturnDefinition;
import zw.co.nbs.returnstrackingsystem.domain.enums.Frequency;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;
import zw.co.nbs.returnstrackingsystem.domain.Submission;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
        // Flexible method that works for both single and multiple statuses
        @Query("SELECT s FROM Submission s WHERE " +
                        "(:status IS NULL OR s.status = :status) " +
                        "AND (:statuses IS NULL OR s.status IN :statuses) " +
                        "AND (:departmentId IS NULL OR s.returnDefinition.department.id = :departmentId) " +
                        "AND s.deleted = false")
        List<Submission> findByStatusAndDepartment(@Param("status") SubmissionStatus status,
                        @Param("statuses") List<SubmissionStatus> statuses,
                        @Param("departmentId") Long departmentId);

        default List<Submission> findByStatusInAndDepartment(List<SubmissionStatus> statuses, Long departmentId) {
                return findByStatusAndDepartment(null, statuses, departmentId);
        }

        @Query("SELECT COUNT(s) FROM Submission s WHERE " +
                        "(:departmentId IS NULL OR s.returnDefinition.department.id = :departmentId) " +
                        "AND s.deleted = false")
        long countByDepartment(@Param("departmentId") Long departmentId);

        @Query("SELECT COUNT(s) FROM Submission s WHERE " +
                        "s.status = :status " +
                        "AND (:departmentId IS NULL OR s.returnDefinition.department.id = :departmentId) " +
                        "AND s.deleted = false")
        long countByStatusAndDepartment(@Param("status") SubmissionStatus status,
                        @Param("departmentId") Long departmentId);

        @Query("SELECT s FROM Submission s WHERE " +
                        "s.status = :status " +
                        "AND (:departmentId IS NULL OR s.returnDefinition.department.id = :departmentId) " +
                        "AND s.deleted = false")
        List<Submission> findByStatusAndDepartment(@Param("status") SubmissionStatus status,
                        @Param("departmentId") Long departmentId);

        @Query("SELECT s FROM Submission s WHERE " +
                        "s.dueAt BETWEEN :startDate AND :endDate " +
                        "AND s.status IN :statuses " +
                        "AND (:departmentId IS NULL OR s.returnDefinition.department.id = :departmentId) " +
                        "AND s.deleted = false " +
                        "ORDER BY s.dueAt ASC")
        List<Submission> findUpcomingByDepartment(@Param("startDate") OffsetDateTime startDate,
                        @Param("endDate") OffsetDateTime endDate,
                        @Param("statuses") List<SubmissionStatus> statuses,
                        @Param("departmentId") Long departmentId);

        List<Submission> findByStatus(SubmissionStatus status);

        @Query("SELECT s FROM Submission s WHERE s.dueAt < :cutoffTime AND s.status IN :statuses AND s.status != 'SUBMITTED'")
        List<Submission> findByDueAtBeforeAndStatusInExcludingSubmitted(
                        @Param("cutoffTime") OffsetDateTime cutoffTime,
                        @Param("statuses") List<SubmissionStatus> statuses);

        @Query("""
                         SELECT s FROM Submission s\s
                         JOIN s.returnDefinition rd\s
                         JOIN rd.department d\s
                         WHERE (:status IS NULL OR s.status = :status)\s
                         AND (:departmentName IS NULL OR d.departmentName = :departmentName)\s
                         AND (:frequency IS NULL OR rd.frequency = :frequency)\s
                         AND (:fromDate IS NULL OR s.dueAt >= :fromDate)\s
                         AND (:toDate IS NULL OR s.dueAt <= :toDate)\s
                         AND (:excludeStatuses IS NULL OR s.status NOT IN :excludeStatuses)\s
                         AND s.deleted = false\s
                         AND rd.deleted = false
                        \s""")
        Page<Submission> getAll(@Param("status") SubmissionStatus status,
                        @Param("departmentName") String departmentName,
                        @Param("frequency") Frequency frequency,
                        @Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate,
                        @Param("excludeStatuses") List<SubmissionStatus> excludeStatuses,
                        Pageable pageable);

        @Query("SELECT s FROM Submission s WHERE " +
                        "s.status IN ('OVERDUE', 'UPLOADED_OVERDUE') " +
                        "AND s.deleted = false")
        List<Submission> findOverdueNotSubmitted();

        @Query("""
                        SELECT s FROM Submission s
                        WHERE s.dueAt BETWEEN :now AND :future
                          AND s.status NOT IN :excludedStatuses
                          AND s.deleted = false
                        """)
        List<Submission> findUpcoming(@Param("now") OffsetDateTime now,
                        @Param("future") OffsetDateTime future,
                        @Param("excludedStatuses") List<SubmissionStatus> excludedStatuses);

        @Query("""
                        SELECT s.returnDefinition.department.departmentName AS department,
                               (SUM(CASE WHEN s.status IN ('CLOSED', 'SUBMITTED') THEN 1 ELSE 0 END) * 1.0 / COUNT(s)) * 100
                        FROM Submission s
                        GROUP BY s.returnDefinition.department.departmentName
                        """)
        List<Object[]> findCompletionRatesByDepartment();

        @Query("SELECT s FROM Submission s WHERE " +
                        "s.returnDefinition = :returnDefinition AND " +
                        "s.periodStart = :periodStart AND " +
                        "s.periodEnd = :periodEnd AND " +
                        "s.dueAt = :dueAt AND " +
                        "s.deleted = false")
        Optional<Submission> findByReturnDefinitionAndPeriodDetails(
                        @Param("returnDefinition") ReturnDefinition returnDefinition,
                        @Param("periodStart") OffsetDateTime periodStart,
                        @Param("periodEnd") OffsetDateTime periodEnd,
                        @Param("dueAt") OffsetDateTime dueAt);
}
