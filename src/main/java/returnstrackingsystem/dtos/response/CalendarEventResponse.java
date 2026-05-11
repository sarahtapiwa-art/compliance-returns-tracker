package returnstrackingsystem.dtos.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class CalendarEventResponse {
    private Long id;
    private String title;
    private String regulatoryBody;
    private String department;
    private String status;
    private String frequency;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
}