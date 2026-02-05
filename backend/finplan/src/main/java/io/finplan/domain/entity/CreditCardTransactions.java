package io.finplan.domain.entity;

import io.finplan.domain.model.enums.CreditCardTransactionCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_card_transactions")
@Getter
@Setter
public class CreditCardTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private CreditCardInvoice creditCardInvoice;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CreditCardTransactionCategory category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "total_purchase_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPurchaseAmount;

    @Column(nullable = false)
    private boolean installments = false;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments = 0;

    @Column(name = "current_installment", nullable = false)
    private Integer currentInstallment = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
