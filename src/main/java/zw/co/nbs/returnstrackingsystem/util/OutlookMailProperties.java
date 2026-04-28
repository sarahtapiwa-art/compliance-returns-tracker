package zw.co.nbs.returnstrackingsystem.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * createdBy romeo
 * createdDate 20/11/2025
 * createdTime 14:51
 * projectName compliance-returns-tracker
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutlookMailProperties {
    private String username;
    private String password;
    private String host = "smtp.office365.com";
    private int port = 587;
    private boolean auth = true;
    private boolean starttls = true;
}
