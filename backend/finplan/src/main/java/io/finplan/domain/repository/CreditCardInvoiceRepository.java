package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.model.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {

    List<CreditCardInvoice> findByCreditCardId(UUID creditCardId);

    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonth(UUID creditCardId,
                                                                    Integer referenceMonth);

    List<CreditCardInvoice> findByCreditCardIdAndStatusIn(UUID creditCardId, List<StatusType> statuses);

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

    Optional<CreditCardInvoice> findByReferenceMonthAndCreditCardId(Integer referenceMonth, UUID creditCardId);

    @Query("""
       SELECT i\s
       FROM CreditCardInvoice i\s
       LEFT JOIN FETCH i.transactions\s
       WHERE i.referenceMonth = :referenceMonth\s
         AND i.creditCard.id = :cardId
      \s""")
    Optional<CreditCardInvoice> findByReferenceMonthAndCreditCardIdWithTransactions(
            @Param("referenceMonth") Integer referenceMonth,
            @Param("cardId") UUID cardId
    );

    @Query("""
        SELECT DISTINCT i.referenceMonth
        FROM CreditCardInvoice i
        JOIN i.creditCard c
        WHERE c.user.id = :userId
        ORDER BY i.referenceMonth DESC
    """)
    List<Integer> findDistinctReferenceMonthsByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT i.referenceMonth
        FROM CreditCardInvoice i
        JOIN i.creditCard c
        WHERE c.user.id = :userId
          AND c.id = :cardId
        ORDER BY i.referenceMonth DESC
    """)
    List<Integer> findDistinctReferenceMonthsByUserIdAndCardId(
            @Param("userId") UUID userId,
            @Param("cardId") UUID cardId
    );
}
