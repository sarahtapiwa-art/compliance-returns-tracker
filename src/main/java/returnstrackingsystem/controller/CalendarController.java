package returnstrackingsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import returnstrackingsystem.dtos.response.CalendarEventResponse;
import returnstrackingsystem.service.SubmissionService;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/calendar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('USER')")
public class CalendarController {

    private final SubmissionService submissionService;

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventResponse>> getCalendarEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) String departmentName) {

        log.info("Getting calendar events from: {}, to: {}, department: {}", from, to, departmentName);
        List<CalendarEventResponse> events = submissionService.getCalendarEvents(from, to, departmentName);
        return ResponseEntity.ok(events);
    }
}