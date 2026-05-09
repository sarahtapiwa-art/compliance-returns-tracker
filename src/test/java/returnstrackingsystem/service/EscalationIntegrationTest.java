package returnstrackingsystem.service;

import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import returnstrackingsystem.domain.Department;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.domain.ScheduleRule;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.repository.DepartmentRepository;
import returnstrackingsystem.repository.ReturnDefinitionRepository;
import returnstrackingsystem.repository.ScheduleRuleRepository;
import returnstrackingsystem.repository.SubmissionRepository;
import returnstrackingsystem.domain.*;
import returnstrackingsystem.domain.enums.Frequency;
import returnstrackingsystem.domain.enums.SubmissionStatus;
import returnstrackingsystem.repository.*;
import returnstrackingsystem.service.impl.NotificationServiceImpl;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class EscalationIntegrationTest {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ReturnDefinitionRepository returnDefinitionRepository;

    @Autowired
    private ScheduleRuleRepository scheduleRuleRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private GraphServiceClient<?> graphClient;

    private Department dept;
    private ReturnDefinition rd;

 //   @BeforeEach
    void setup() {
        dept = departmentRepository.save(Department.builder()
                .departmentName("Test Dept")
                .escalationEmail("escalate@nbs.co.zw")
                .build());

        rd = returnDefinitionRepository.save(ReturnDefinition.builder()
                .title("Test Report")
                .department(dept)
                .frequency(Frequency.MONTHLY)
                .active(true)
                .build());

        scheduleRuleRepository.save(ScheduleRule.builder()
                .returnDefinition(rd)
                .escalateAfterHours(1)
                .build());
    }

//    @Test
    void testEscalateOverdueSubmission() {
        OffsetDateTime dueAt = OffsetDateTime.now(ZoneId.of("Africa/Harare")).minusHours(2);
        submissionRepository.save(Submission.builder()
                .returnDefinition(rd)
                .dueAt(dueAt)
                .status(SubmissionStatus.OVERDUE)
                .build());

        notificationService.escalateOverdue();

        verify(emailService, times(1)).send(any(), eq("ian.katuli@nbs.co.zw"), any(), contains("Overdue:"), any());
    }

//    @Test
    void testEscalateUploadedOverdueSubmission() {
        OffsetDateTime dueAt = OffsetDateTime.now(ZoneId.of("Africa/Harare")).minusHours(2);
        submissionRepository.save(Submission.builder()
                .returnDefinition(rd)
                .dueAt(dueAt)
                .status(SubmissionStatus.UPLOADED_OVERDUE)
                .build());

        notificationService.escalateOverdue();

        verify(emailService, times(1)).send(any(), eq("escalate@nbs.co.zw"), any(), contains("Overdue:"), any());
    }
}
