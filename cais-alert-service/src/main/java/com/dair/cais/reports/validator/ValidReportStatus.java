package com.dair.cais.reports.validator;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReportStatusValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReportStatus {
    String message() default "Invalid report status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
