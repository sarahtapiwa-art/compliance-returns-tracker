package zw.co.nbs.returnstrackingsystem.customvalidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * createdBy romeo
 * createdDate 19/11/2025
 * createdTime 09:58
 * projectName compliance-returns-tracker
 **/

@Documented
@Constraint(validatedBy = NbsEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NbsEmail {
    String message() default "Email must be from these domains: [@nbs.co.zw, @lenderspark.co.zw]";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
