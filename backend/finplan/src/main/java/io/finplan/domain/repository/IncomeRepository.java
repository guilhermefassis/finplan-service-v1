package io.finplan.domain.repository;

import io.finplan.domain.entity.Incomes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Incomes, UUID> {

    List<Incomes> findByUserId(UUID userId);

    List<Incomes> findByUserIdAndReceivedDateBetween(UUID userId, LocalDate start, LocalDate end);

    List<Incomes> findByUserIdAndRecurringTrue(UUID userId);

    List<Incomes> findByUserIdAndType(UUID userId, String type);
}