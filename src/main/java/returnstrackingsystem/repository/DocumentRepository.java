package returnstrackingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import returnstrackingsystem.domain.Document;
import returnstrackingsystem.domain.Submission;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findBySubmission(Submission submission);

    Optional<Document> findTopBySubmissionOrderByUploadedAtDesc(Submission submission);

    @Modifying
    @Query("UPDATE Document d SET d.submission = NULL WHERE d.submission = :submission")
    void detachDocumentsFromSubmission(@Param("submission") Submission submission);

    // For verification
    @Query("SELECT COUNT(d) FROM Document d WHERE d.submission = :submission")
    long countBySubmission(@Param("submission") Submission submission);
}