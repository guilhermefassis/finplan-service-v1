package io.finplan.domain.entity;

import io.finplan.domain.model.enums.FixedExpenseCategoryEnum;
import io.finplan.domain.model.enums.PaymentMethodEnum;
import io.finplan.domain.model.enums.RecurrenceTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fixed_expenses")
@Getter
@Setter
public class FixedExpenses {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FixedExpenseCategoryEnum category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private Integer dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurrenceTypeEnum recurrence;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethodEnum paymentMethod;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
