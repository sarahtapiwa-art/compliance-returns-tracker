package zw.co.nbs.returnstrackingsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * createdBy       lorraine.mhizha
 * createdDate     22/8/2025
 * createdTime     11:03
 * projectName     compliance-returns-tracker
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    private ReturnDefinition returnDefinition;
    private Integer remindDaysBefore;
    private Integer escalateAfterHours;
}
