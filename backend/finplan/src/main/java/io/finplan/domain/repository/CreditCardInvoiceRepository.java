package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCardInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {
    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonth(UUID creditCardId,
                                                                    Integer referenceMonth);
}
