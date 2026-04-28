package zw.co.nbs.returnstrackingsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zw.co.nbs.returnstrackingsystem.domain.Submission;
import zw.co.nbs.returnstrackingsystem.domain.TaskTrack;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;

import java.util.Optional;

public interface TaskTrackRepository extends JpaRepository<TaskTrack, Long> {

    Optional<TaskTrack> findBySubmission(Submission submission);
    @Query("SELECT tt FROM TaskTrack tt " +
            "JOIN FETCH tt.submission s " +
            "JOIN FETCH s.returnDefinition rd " +
            "JOIN FETCH rd.department d " +
            "WHERE (:userEmail IS NULL OR tt.userEmail = :userEmail) " +
            "AND (:completed IS NULL OR tt.completed = :completed) " +
            "AND (:submissionStatus IS NULL OR s.status = :submissionStatus) " +
            "AND (:departmentName IS NULL OR d.departmentName = :departmentName)")
    Page<TaskTrack> findAllWithFilters(@Param("userEmail") String userEmail,
                                       @Param("completed") Boolean completed,
                                       @Param("submissionStatus") SubmissionStatus submissionStatus,
                                       @Param("departmentName") String departmentName,
                                       Pageable pageable);

    Page<TaskTrack> findAllBySubmission_ReturnDefinition_Department_DepartmentName(String departmentName, Pageable pageable);

    Page<TaskTrack> findByUserEmail(String email, Boolean completed, Pageable pageable);

    long countByUserEmail(String email);
}
