package io.finplan.api.dto.invoice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.finplan.api.dto.transactions.ResponseCreditCardTransactionDTO;
import io.finplan.domain.model.enums.StatusType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseInvoiceDTO(
        UUID id,
        UUID creditCardId,
        Integer referenceMonth,
        LocalDate closingDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        StatusType status,
        LocalDate paymentDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ResponseCreditCardTransactionDTO> transactions
) {
}
