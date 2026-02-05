package io.finplan.api.dto.balance;

import io.finplan.domain.model.enums.CreditCardTransactionCategory;
import java.math.BigDecimal;

public record CategoryExpenseDetailDTO(
        CreditCardTransactionCategory category,
        String categoryName,
        BigDecimal monthlyAmount,
        BigDecimal totalAccumulatedAmount,
        Double percentageInMonth,
        Integer transactionCount
) {}