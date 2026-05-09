package returnstrackingsystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 26/8/2025
 * createdTime 13:44
 * projectName compliance-returns-tracker
 **/

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;
    private String token;
    private LocalDateTime expiryDate;

    @OneToOne
    private User user;
}

