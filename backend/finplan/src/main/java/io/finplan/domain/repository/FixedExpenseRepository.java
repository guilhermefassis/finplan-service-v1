package io.finplan.domain.repository;

import io.finplan.domain.entity.FixedExpenses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FixedExpenseRepository extends JpaRepository<FixedExpenses, UUID> {
    List<FixedExpenses> findByUserId(UUID userId);

    List<FixedExpenses> findByUserIdAndActiveTrue(UUID userId);

    List<FixedExpenses> findByUserIdAndCategory(UUID userId, String category);

    List<FixedExpenses> findByUserIdAndDueDay(UUID userId, Integer dueDay);
}