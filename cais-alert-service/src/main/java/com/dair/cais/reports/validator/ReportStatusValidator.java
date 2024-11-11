package com.dair.cais.reports.validator;

import com.dair.cais.reports.constant.ReportConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ReportStatusValidator implements ConstraintValidator<ValidReportStatus, String> {
    private static final Set<String> VALID_STATUSES = Set.of(
            ReportConstants.Status.DRAFT,
            ReportConstants.Status.PUBLISHED,
            ReportConstants.Status.ARCHIVED
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || VALID_STATUSES.contains(value);
    }
}