package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCardInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {
    List<CreditCardInvoice> findByCreditCardId(UUID creditCardId);

    List<CreditCardInvoice> findByCreditCardIdAndStatus(UUID creditCardId,
                                                        String status);
    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonth(UUID creditCardId,
                                                                    LocalDate referenceMonth);
    List<CreditCardInvoice> findByDueDateBetween(LocalDate startDate,
                                                 LocalDate endDate);
    List<CreditCardInvoice> findByCreditCardIdAndDueDateBetween(UUID creditCardId,
                                                                LocalDate startDate,
                                                                LocalDate endDate);
    List<CreditCardInvoice> findByStatus(String status);
}
