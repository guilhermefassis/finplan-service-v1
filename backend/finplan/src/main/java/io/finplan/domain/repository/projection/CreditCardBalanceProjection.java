package io.finplan.domain.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditCardBalanceProjection {
    UUID getCardId();
    String getCardName();
    String getBrand();
    BigDecimal getCreditLimit();
    UUID getInvoiceId();
    Integer getReferenceMonth();
    LocalDate getClosingDate();
    LocalDate getDueDate();
    String getStatus();
    BigDecimal getTotalAmount();
    LocalDate getPaymentDate();
}