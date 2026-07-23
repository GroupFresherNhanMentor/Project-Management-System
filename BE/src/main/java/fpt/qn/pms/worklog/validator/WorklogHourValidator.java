package fpt.qn.pms.worklog.validator;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WorklogHourValidator implements ConstraintValidator<ValidWorklogHour, BigDecimal> {

    private static final BigDecimal MIN_HOUR = BigDecimal.ZERO;
    private static final BigDecimal MAX_HOUR = new BigDecimal("24.0");

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation if specified
        }
        return value.compareTo(MIN_HOUR) > 0 && value.compareTo(MAX_HOUR) <= 0;
    }
}
