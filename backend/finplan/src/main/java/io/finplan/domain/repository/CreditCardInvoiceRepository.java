package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCardInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {
    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonth(UUID creditCardId,
                                                                    Integer referenceMonth);

    Optional<CreditCardInvoice> findByIdAndCreditCardId(UUID id, UUID creditCardId);

    @Query("""
       SELECT i\s
       FROM CreditCardInvoice i\s
       LEFT JOIN FETCH i.transactions\s
       WHERE i.id = :id\s
         AND i.creditCard.id = :cardId
      \s""")
    Optional<CreditCardInvoice> findByIdAndCreditCardIdWithTransactions(
            @Param("id") UUID id,
            @Param("cardId") UUID cardId
    );
}
