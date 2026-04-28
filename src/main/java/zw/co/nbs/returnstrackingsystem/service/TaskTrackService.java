package zw.co.nbs.returnstrackingsystem.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zw.co.nbs.returnstrackingsystem.domain.TaskTrack;
import zw.co.nbs.returnstrackingsystem.domain.enums.SubmissionStatus;

import java.security.Principal;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 14:40
 * projectName compliance-returns-tracker
 **/

public interface TaskTrackService{
    Page<TaskTrack> getTaskTracks(Principal currentUser,
                                  Pageable pageable) ;
}
