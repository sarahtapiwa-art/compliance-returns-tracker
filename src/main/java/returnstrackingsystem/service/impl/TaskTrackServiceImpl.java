package returnstrackingsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import returnstrackingsystem.domain.TaskTrack;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.exception.RecordNotFoundException;
import returnstrackingsystem.repository.TaskTrackRepository;
import returnstrackingsystem.repository.UserRepository;
import returnstrackingsystem.service.TaskTrackService;

import java.security.Principal;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 14:41
 * projectName compliance-returns-tracker
 **/

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTrackServiceImpl implements TaskTrackService {

    private final TaskTrackRepository taskTrackRepository;
    private final UserRepository userRepository;

    @Value("${superadmin.role}")
    private String superAdminUserRole;

    @Override
    public Page<TaskTrack> getTaskTracks(Principal currentUser,
                                         Pageable pageable) {

        log.info("Getting all tasks");
        User user = userRepository.findByUsername(currentUser.getName())
                .orElseThrow(() -> new RecordNotFoundException("User not found"));

        boolean isSuperAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority()
                        .equals(superAdminUserRole));

         if (isSuperAdmin) {
             return taskTrackRepository.findAll(pageable);
         }

        String departmentName = user.getDepartment().getDepartmentName();
        return taskTrackRepository
                .findAllBySubmission_ReturnDefinition_Department_DepartmentName(
                        departmentName, pageable
                );
    }
}