package com.dduru.gildongmu.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class NoNullElementsValidator implements ConstraintValidator<NoNullElements, Collection<?>> {

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int index = 0;
        for (Object element : value) {
            if (element == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate() + " (index=" + index + ")"
                ).addConstraintViolation();
                return false;
            }
            index++;
        }
        return true;
    }
}
