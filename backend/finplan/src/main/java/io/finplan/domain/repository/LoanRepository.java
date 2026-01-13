package io.finplan.domain.repository;

import io.finplan.domain.entity.Loans;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loans, UUID> {

    List<Loans> findByUserId(UUID userId);

    List<Loans> findByUserIdAndActiveTrue(UUID userId);

    List<Loans> findByUserIdAndInstitution(UUID userId, String institution);

    List<Loans> findByUserIdAndDueDate(UUID userId, Integer dueDate);
}