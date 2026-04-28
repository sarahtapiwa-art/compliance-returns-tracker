package zw.co.nbs.returnstrackingsystem.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import zw.co.nbs.returnstrackingsystem.domain.Submission;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;
import zw.co.nbs.returnstrackingsystem.repository.SubmissionRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionClosingJob {

    private final SubmissionRepository submissionRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void closeSubmittedReturns() {
        log.info("Starting scheduled job: Auto-closing SUBMITTED returns...");

        List<Submission> submittedReturns = submissionRepository.findByStatus(SubmissionStatus.SUBMITTED);

        if (submittedReturns.isEmpty()) {
            log.info("No SUBMITTED returns found to close.");
            return;
        }

        int count = 0;
        for (Submission submission : submittedReturns) {
            try {
                submission.setStatus(SubmissionStatus.CLOSED);
                count++;
            } catch (Exception e) {
                log.error("Failed to close submission with ID: {}", submission.getId(), e);
            }
        }

        submissionRepository.saveAll(submittedReturns);
        log.info("Successfully closed {} submissions.", count);
    }
}
