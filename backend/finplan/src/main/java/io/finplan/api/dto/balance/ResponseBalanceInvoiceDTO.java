package io.finplan.api.dto.balance;

import io.finplan.domain.model.enums.StatusType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ResponseBalanceInvoiceDTO (
        UUID id,
        Integer referenceMonth,
        LocalDate closingDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        StatusType status,
        LocalDate paymentDate
) {
}
