package com.dduru.gildongmu.survey.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InterestListValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInterestList {
    String message() default "유효하지 않은 관심사입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
