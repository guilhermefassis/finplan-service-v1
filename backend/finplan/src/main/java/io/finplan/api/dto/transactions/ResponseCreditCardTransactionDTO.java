package io.finplan.api.dto.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.finplan.domain.model.enums.CreditCardTransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


public record ResponseCreditCardTransactionDTO(
        UUID id,
        UUID creditCardId,
        UUID invoiceId,
        UUID groupId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate purchaseDate,
        String description,
        CreditCardTransactionCategory category,
        BigDecimal amount,
        Boolean installments,
        Integer totalInstallments,
        Integer currentInstallment,
        BigDecimal totalPurchaseAmount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        OffsetDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        OffsetDateTime updatedAt
) {}