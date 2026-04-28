package zw.co.nbs.returnstrackingsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.co.nbs.returnstrackingsystem.convertor.NotificationLogObjectMapper;
import zw.co.nbs.returnstrackingsystem.dtos.response.NotificationLogResponse;
import zw.co.nbs.returnstrackingsystem.dtos.response.PagedResponse;
import zw.co.nbs.returnstrackingsystem.service.NotificationLogService;
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
 * createdTime 14:00
 * projectName compliance-returns-tracker
 **/

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notification-log")
@Tag(name = "Notification Logs", description = "Endpoints for managing notification logs")
@PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('USER')")
public class NotificationLogRestController {

    private final NotificationLogService notificationLogService;
    private final NotificationLogObjectMapper notificationLogObjectMapper;

    @GetMapping
    @Operation(
            summary = "Retrieve paginated notification logs",
            description = "Fetches all notification logs with pagination support.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved list",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PagedResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<PagedResponse<NotificationLogResponse>> getNotificationLogs(
            @Parameter(description = "Pagination information")
            @PageableDefault(direction = DESC, sort = "id") Pageable pageable,
            Principal currentUser) {

        var page = notificationLogService.getNotificationLogs(currentUser, pageable)
                .map(notificationLogObjectMapper::toNotificationLog);

        return ResponseEntity.ok(
                PagedResponse.<NotificationLogResponse>builder()
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