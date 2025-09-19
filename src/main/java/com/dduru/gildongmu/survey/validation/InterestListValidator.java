package com.dduru.gildongmu.survey.validation;

import com.dduru.gildongmu.survey.domain.enums.Interest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InterestListValidator implements ConstraintValidator<ValidInterestList, List<String>> {

    private Set<String> validInterests;

    @Override
    public void initialize(ValidInterestList constraintAnnotation) {
        validInterests = Arrays.stream(Interest.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.stream().allMatch(validInterests::contains);
    }
}
