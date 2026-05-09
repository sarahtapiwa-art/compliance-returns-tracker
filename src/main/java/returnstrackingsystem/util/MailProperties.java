package returnstrackingsystem.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * createdBy       lorraine.mhizha
 * createdDate     25/8/2025
 * createdTime     11:09
 * projectName     compliance-returns-tracker
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailProperties {
    private String username;
    private String password;
    private String subject;
}

