package io.finplan.api.dto.balance;

import java.math.BigDecimal;
import java.util.UUID;

public record ResponseBalanceCreditCardDTO(
        UUID id,
        String name,
        String brand,
        BigDecimal creditLimit,
        ResponseBalanceInvoiceDTO invoice
) {
}
