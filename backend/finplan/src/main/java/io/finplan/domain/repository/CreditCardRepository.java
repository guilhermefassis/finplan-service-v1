package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.repository.projection.CategoryExpenseProjection;
import io.finplan.domain.repository.projection.CategoryTotalProjection;
import io.finplan.domain.repository.projection.CreditCardBalanceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    Optional<CreditCard> findByIdAndUserId(UUID id, UUID userId);

    List<CreditCard> findByUserIdAndActive(UUID userId, boolean active);

    @Query(value = """
        SELECT
          c.id AS card_id,
          c.name AS card_name,
          c.brand,
          c.credit_limit,
          i.id AS invoice_id,
          i.reference_month,
          i.closing_date,
          i.due_date,
          i.status,
          i.total_amount,
          i.payment_date
        FROM credit_cards c
        LEFT JOIN credit_card_invoices i
          ON i.credit_card_id = c.id
         AND i.reference_month = :referenceMonth
        WHERE c.user_id = :userId
          AND c.active = true
        ORDER BY c.name, i.due_date
        """, nativeQuery = true)
    List<CreditCardBalanceProjection> findMonthlyBalanceByUserAndMonth(
            @Param("userId") UUID userId,
            @Param("referenceMonth") Integer referenceMonth
    );
}

