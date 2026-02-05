package io.finplan.domain.repository.projection;

import java.math.BigDecimal;

public interface CategoryTotalProjection {
    String getCategory();
    BigDecimal getTotalAmount();
}
