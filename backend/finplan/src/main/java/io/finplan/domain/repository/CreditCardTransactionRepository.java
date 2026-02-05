package io.finplan.domain.repository;


import io.finplan.domain.entity.CreditCardTransactions;
import io.finplan.domain.repository.projection.CategoryExpenseProjection;
import io.finplan.domain.repository.projection.CategoryTotalProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransactions, UUID> {
    List<CreditCardTransactions> findByGroupIdAndCreditCard_Id(UUID groupId, UUID cardId);
    List<CreditCardTransactions> findByCreditCardId(UUID creditCardId);
    @Modifying
    @Transactional
    void deleteByGroupId(UUID groupId);

    @Query("""
        SELECT\s
            t.category as category,
            SUM(t.amount) as monthlyAmount,
            COUNT(t.id) as transactionCount
        FROM CreditCardTransactions t
        JOIN t.creditCardInvoice i
        WHERE t.creditCard.user.id = :userId
        AND i.referenceMonth = :referenceMonth
        GROUP BY t.category
        ORDER BY monthlyAmount DESC
   \s""")
    List<CategoryExpenseProjection> findMonthlyExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("referenceMonth") Integer referenceMonth
    );

    @Query("""
        SELECT\s
            t.category as category,
            SUM(t.amount) as monthlyAmount,
            COUNT(t.id) as transactionCount
        FROM CreditCardTransactions t
        JOIN t.creditCardInvoice i
        WHERE t.creditCard.user.id = :userId
        GROUP BY t.category
        ORDER BY monthlyAmount DESC
   \s""")
    List<CategoryExpenseProjection> findTotalExpensesByCategory(
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT\s
            t.category as category,
            SUM(t.amount) as totalAmount
        FROM CreditCardTransactions t
        WHERE t.creditCard.user.id = :userId
        GROUP BY t.category
   \s""")
    List<CategoryTotalProjection> findTotalAccumulatedExpensesByCategory(
            @Param("userId") UUID userId
    );


    @Query("""
        SELECT\s
            t.category as category,
            SUM(t.amount) as totalAmount
        FROM CreditCardTransactions t
        WHERE t.creditCard.user.id = :userId
        AND t.purchaseDate <= :endDate
        GROUP BY t.category
   \s""")
    List<CategoryTotalProjection> findTotalAccumulatedExpensesByCategory(
            @Param("userId") UUID userId,
            @Param("endDate") LocalDate endDate
    );
}