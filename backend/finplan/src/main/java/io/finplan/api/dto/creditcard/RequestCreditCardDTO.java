package io.finplan.api.dto.creditcard;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RequestCreditCardDTO(
        @NotBlank(message = "The name of credit card is required!")
        String name,

        @NotBlank(message = "The brand is required!")
        String brand,

        @NotNull(message = "Closing day is required!")
        @Min(1) @Max(31)
        Integer closingDay,

        @NotNull(message = "Due day is required!")
        @Min(1) @Max(31)
        Integer dueDay,

        @NotNull(message = "Credit card limit is required!")
        @DecimalMin("0.00")
        BigDecimal creditLimit
) {}