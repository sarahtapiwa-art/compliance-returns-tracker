package zw.co.nbs.returnstrackingsystem.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zw.co.nbs.returnstrackingsystem.customvalidation.NbsEmail;


/**
 * createdBy romeo
 * createdDate 10/2/2026
 * createdTime 07:50
 * projectName compliance-returns-tracker
 **/

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsiblePerson {

    @Column(name = "responsible_name", nullable = false, length = 50)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(name = "responsible_surname", nullable = false, length = 50)
    @NotBlank(message = "Surname is required")
    private String surname;

    @Column(name = "responsible_email", nullable = false)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @NbsEmail
    private String email;
}
