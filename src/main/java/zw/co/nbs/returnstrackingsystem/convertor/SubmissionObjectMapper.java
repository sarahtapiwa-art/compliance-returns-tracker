package zw.co.nbs.returnstrackingsystem.convertor;

import org.mapstruct.*;
import zw.co.nbs.returnstrackingsystem.dtos.request.SubmissionRequest;
import zw.co.nbs.returnstrackingsystem.dtos.response.SubmissionResponse;
import zw.co.nbs.returnstrackingsystem.domain.Submission;

@Mapper(componentModel = "spring")
public interface SubmissionObjectMapper {

    SubmissionResponse toSubmissionResponse(Submission submission);

}
