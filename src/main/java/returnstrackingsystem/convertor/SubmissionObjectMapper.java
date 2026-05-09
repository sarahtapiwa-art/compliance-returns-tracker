package returnstrackingsystem.convertor;

import org.mapstruct.*;
import returnstrackingsystem.dtos.response.SubmissionResponse;
import returnstrackingsystem.domain.Submission;

@Mapper(componentModel = "spring")
public interface SubmissionObjectMapper {

    SubmissionResponse toSubmissionResponse(Submission submission);

}
