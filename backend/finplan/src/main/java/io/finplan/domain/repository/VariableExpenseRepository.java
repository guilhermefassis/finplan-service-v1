package io.finplan.domain.repository;

import io.finplan.domain.entity.VariableExpenses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VariableExpenseRepository extends JpaRepository<VariableExpenses, UUID> {

    List<VariableExpenses> findByUserId(UUID userId);

    List<VariableExpenses> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end);

    List<VariableExpenses> findByUserIdAndCategory(UUID userId, String category);

    List<VariableExpenses> findByUserIdAndPaymentMethod(UUID userId, String paymentMethod);
}