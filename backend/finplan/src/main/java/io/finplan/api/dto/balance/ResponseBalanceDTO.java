package io.finplan.api.dto.balance;

import java.math.BigDecimal;
import java.util.List;

public record ResponseBalanceDTO(
        Integer referenceMonth,
        BigDecimal totalInvoicesAmount,
        BigDecimal totalCreditCardsAmount,
        List<ResponseBalanceCreditCardDTO> cards
) {}
