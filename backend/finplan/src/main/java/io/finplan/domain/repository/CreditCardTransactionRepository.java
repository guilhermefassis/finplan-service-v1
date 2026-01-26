package io.finplan.domain.repository;


import io.finplan.domain.entity.CreditCardTransactions;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransactions, UUID> {
    List<CreditCardTransactions> findByGroupId(UUID groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(UUID groupId);
}