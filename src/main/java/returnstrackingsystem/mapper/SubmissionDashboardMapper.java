package returnstrackingsystem.mapper;

import returnstrackingsystem.domain.Submission;
import returnstrackingsystem.dtos.response.SubmissionDashboardResponse;

public class SubmissionDashboardMapper {

    public static SubmissionDashboardResponse toResponse(Submission submission) {
        return SubmissionDashboardResponse.builder()
                .id(submission.getId())
                .title(submission.getReturnDefinition().getTitle())
                .department(submission.getReturnDefinition()
                        .getDepartment()
                        .getDepartmentName())
                .dueAt(submission.getDueAt())
                .status(submission.getStatus())
                .periodLabel(submission.periodLabel())
                .build();
    }
}
