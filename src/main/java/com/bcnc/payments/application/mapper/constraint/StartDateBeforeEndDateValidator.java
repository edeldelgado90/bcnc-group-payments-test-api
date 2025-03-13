package com.bcnc.payments.application.mapper.constraint;

import com.bcnc.payments.application.mapper.dto.PriceDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StartDateBeforeEndDateValidator
        implements ConstraintValidator<StartDateBeforeEndDate, PriceDTO> {

    @Override
    public boolean isValid(PriceDTO price, ConstraintValidatorContext context) {
        if (price.getStartDate() == null || price.getEndDate() == null) {
            return true;
        }
        return price.getStartDate().isBefore(price.getEndDate());
    }
}
