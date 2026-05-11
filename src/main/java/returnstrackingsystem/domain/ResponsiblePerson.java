package returnstrackingsystem.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import returnstrackingsystem.customvalidation.Email;

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
    @NotBlank(message = "Email is required")
    @Email
    private String email;
}