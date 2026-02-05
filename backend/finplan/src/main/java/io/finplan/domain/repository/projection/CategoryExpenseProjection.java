package io.finplan.domain.repository.projection;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {
    String getCategory();
    BigDecimal getMonthlyAmount();
    Long getTransactionCount();
}
