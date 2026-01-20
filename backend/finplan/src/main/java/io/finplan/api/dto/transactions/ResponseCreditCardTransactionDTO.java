package io.finplan.api.dto.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public record ResponseCreditCardTransactionDTO(
        UUID id,
        UUID creditCardId,
        UUID invoiceId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate purchaseDate,
        String description,
        String category,
        BigDecimal amount,
        Boolean installments,
        Integer totalInstallments,
        Integer currentInstallment,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {}