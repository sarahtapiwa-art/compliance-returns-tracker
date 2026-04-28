package zw.co.nbs.returnstrackingsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.co.nbs.returnstrackingsystem.convertor.TaskTrackObjectMapper;
import zw.co.nbs.returnstrackingsystem.dtos.response.PagedResponse;
import zw.co.nbs.returnstrackingsystem.dtos.response.TaskTrackResponse;
import zw.co.nbs.returnstrackingsystem.service.TaskTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * createdBy romeo
 * createdDate 29/8/2025
 * createdTime 14:44
 * projectName compliance-returns-tracker
 **/

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/task-track")
@Tag(name = "Task Tracks", description = "Endpoints for managing task tracking")
@PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('USER')")
public class TaskTrackRestController {

    private final TaskTrackService taskTrackService;
    private final TaskTrackObjectMapper taskTrackObjectMapper;

    @GetMapping
    @Operation(
            summary = "Retrieve all task tracks",
            description = "Fetches a paginated list of all task tracks.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<PagedResponse<TaskTrackResponse>> getAllTaskTracks(
            @Parameter(description = "Pagination information")
            @PageableDefault(direction = DESC, sort = "id") Pageable pageable,
            Principal currentUser) {

        var page = taskTrackService.getTaskTracks(currentUser, pageable)
                .map(taskTrackObjectMapper::toTaskTrackResponse);

        return ResponseEntity.ok(
                PagedResponse.<TaskTrackResponse>builder()
                        .content(page.getContent())
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .last(page.isLast())
                        .build()
        );
    }
}

