package io.finplan.api.dto.creditcard;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateCreditCardDTO(
        String name,
        String brand,
        @Min(1) @Max(31)
        Integer closingDay,
        @Min(1) @Max(31)
        Integer dueDay,
        @DecimalMin("0.00")
        BigDecimal creditLimit
) {}
