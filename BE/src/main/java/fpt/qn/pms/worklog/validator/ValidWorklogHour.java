package fpt.qn.pms.worklog.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = WorklogHourValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWorklogHour {

    String message() default "Hour must be greater than 0 and less than or equal to 24";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
