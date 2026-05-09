package returnstrackingsystem.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String departmentName;
    @Email
    private String escalationEmail;
    @Email
    private String headOfDepartmentEmail;
    @Builder.Default
    private boolean deleted = false;
}