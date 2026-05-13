package returnstrackingsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.repository.SubmissionRepository;
import returnstrackingsystem.service.impl.RiskAssessmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/risk")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('USER')")
public class RiskController {

    private final RiskAssessmentService riskAssessmentService;
    private final SubmissionRepository submissionRepository;

    @GetMapping("/submissions")
    public ResponseEntity<List<Map<String, Object>>> getAllSubmissionsWithRisk() {
        List<Submission> submissions = submissionRepository.findAll().stream()
                .filter(s -> !s.isDeleted())
                .toList();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Submission s : submissions) {
            try {
                String dueAt = s.getDueAt() != null ? s.getDueAt().toString() : null;
                String frequency = s.getReturnDefinition().getFrequency().name();
                String status = s.getStatus().name();

                Map<String, Object> risk = riskAssessmentService.assessRisk(dueAt, frequency, status, 0.0);
                risk.put("id", s.getId());
                risk.put("title", s.getReturnDefinition().getTitle());
                risk.put("department", s.getReturnDefinition().getDepartment().getDepartmentName());
                risk.put("dueAt", dueAt);
                risk.put("status", status);
                results.add(risk);
            } catch (Exception e) {
                log.warn("Failed to assess risk for submission {}: {}", s.getId(), e.getMessage());
            }
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/submission/{id}")
    public ResponseEntity<Map<String, Object>> getSubmissionRisk(@PathVariable Long id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        String dueAt = s.getDueAt() != null ? s.getDueAt().toString() : null;
        String frequency = s.getReturnDefinition().getFrequency().name();
        String status = s.getStatus().name();

        Map<String, Object> risk = riskAssessmentService.assessRisk(dueAt, frequency, status, 0.0);
        risk.put("id", s.getId());
        risk.put("title", s.getReturnDefinition().getTitle());
        risk.put("department", s.getReturnDefinition().getDepartment().getDepartmentName());
        risk.put("dueAt", dueAt);
        risk.put("status", status);

        return ResponseEntity.ok(risk);
    }
}