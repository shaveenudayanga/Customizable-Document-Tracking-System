package com.docutrace.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { ValidPasswordValidator.class })
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidPassword {
    String message() default "Password does not meet policy";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
