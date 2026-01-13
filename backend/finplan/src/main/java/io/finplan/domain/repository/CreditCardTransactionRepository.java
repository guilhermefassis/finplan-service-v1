package io.finplan.domain.repository;


import io.finplan.domain.entity.CreditCardTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransactions, UUID> {
    List<CreditCardTransactions> findByCreditCardId(UUID creditCardId);

    List<CreditCardTransactions> findByCreditCardInvoice(UUID creditCardInvoice);

    List<CreditCardTransactions> findByCreditCardIdAndPurchaseDateBetween(UUID creditCardId,
                                                                          LocalDate start,
                                                                          LocalDate end);
    List<CreditCardTransactions> findByCreditCardIdAndCategory(UUID creditCardId,
                                                               String category);
    List<CreditCardTransactions> findByCreditCardIdAndInstallmentsTrue(UUID creditCardId);
}