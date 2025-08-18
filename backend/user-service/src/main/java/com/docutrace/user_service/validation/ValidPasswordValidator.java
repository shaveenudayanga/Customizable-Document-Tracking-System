package com.docutrace.user_service.validation;

import com.docutrace.user_service.security.SecurityPasswordProperties;
import com.docutrace.user_service.tenant.TenantContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Autowired(required = false)
    private SecurityPasswordProperties props;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        int min = 8; // default
        if (props != null) {
            min = props.getMinLength();
            String tenant = TenantContext.getTenant();
            if (tenant != null && props.getTenants() != null) {
                var t = props.getTenants().get(tenant);
                if (t != null && t.getMinLength() != null) {
                    min = t.getMinLength();
                }
            }
        }
        if (value.length() < min) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must be at least " + min + " characters long").addConstraintViolation();
            return false;
        }
        return true;
    }
}
