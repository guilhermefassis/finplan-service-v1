package io.finplan.api.dto.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.finplan.domain.model.enums.CreditCardTransactionCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record RequestCreditCardTransactionDTO(

        @NotNull(message = "Credit card ID is required")
        UUID creditCardId,

        @NotNull(message = "Purchase date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate purchaseDate,

        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Category is required")
        CreditCardTransactionCategory category,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Installments flag is required")
        Boolean installments,

        @Min(value = 1, message = "Total installments must be at least 1")
        @Max(value = 48, message = "Total installments must not exceed 48")
        Integer totalInstallments,

        @Min(value = 1, message = "Current installment must be at least 1")
        Integer currentInstallment
) {}