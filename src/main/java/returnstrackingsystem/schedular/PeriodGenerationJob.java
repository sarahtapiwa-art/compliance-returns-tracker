package returnstrackingsystem.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.repository.ReturnDefinitionRepository;
import returnstrackingsystem.repository.SubmissionRepository;

import java.time.*;

import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static java.time.OffsetDateTime.from;
import static returnstrackingsystem.util.TimeUtil.harareLocalToUtc;

/**
 * createdBy romeo
 * createdDate 20/10/2025
 * createdTime 08:09
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodGenerationJob {

    private final ReturnDefinitionRepository definitionRepository;
    private final SubmissionRepository submissionRepository;

    @Scheduled(cron = "0 0 1 * * *", zone = "Africa/Harare")
    @Transactional
    public void generateNextPeriods() {
        log.info("Generating next periods");
        for (ReturnDefinition def : definitionRepository.findAll()) {
            if (!def.isActive()) continue;
            var nowHarare = OffsetDateTime.now(ZoneId.of("Africa/Harare"));
            var start = nowHarare.toLocalDate()
                    .atStartOfDay(ZoneId.of("Africa/Harare"))
                    .toOffsetDateTime();
            var due = harareLocalToUtc(start, valueOf(def.getSubmissionDeadline()));

            Submission submission = Submission.builder()
                    .returnDefinition(def)
                    .periodStart(start)
                    .periodEnd(periodEnd(start, def.getFrequency()))
                    .dueAt(due)
                    .status(SubmissionStatus.PENDING)
                    .build();
            submissionRepository.save(submission);
        }
    }

    private OffsetDateTime periodEnd(OffsetDateTime start, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> start.plusDays(1);
            case WEEKLY -> start.plusWeeks(1);
            case MONTHLY -> start.plusMonths(1);
            case QUARTERLY -> start.plusMonths(3);
            case SEMI_ANNUAL -> start.plusMonths(6);
            case YEARLY -> start.plusYears(1);
        };
    }

    private boolean isSubmissionOverdue(Submission submission) {
        return submission.getStatus() == SubmissionStatus.UPLOADED &&
                submission.getDueAt() != null &&
                submission.getDueAt().isBefore(from(LocalDateTime.now().minusHours(1)));
    }
}