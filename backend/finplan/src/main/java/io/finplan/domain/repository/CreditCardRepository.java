package io.finplan.domain.repository;

import io.finplan.domain.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByUserId(UUID userId);
    List<CreditCard> findByUserIdAndIsActive(UUID userId, boolean active);
    List<CreditCard> findByUserIdAndBrand(UUID userId, String brand);
}
