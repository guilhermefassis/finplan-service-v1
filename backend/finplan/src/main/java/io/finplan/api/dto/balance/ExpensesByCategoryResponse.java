package io.finplan.api.dto.balance;

import java.math.BigDecimal;
import java.util.List;

public record ExpensesByCategoryResponse(
        Integer referenceMonth,
        String currency,
        BigDecimal totalAmount,
        List<CategoryExpenseDetailDTO> categories
) {}