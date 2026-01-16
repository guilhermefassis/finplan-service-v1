package io.finplan.domain.entity;

import io.finplan.domain.model.enums.PaymentFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private UUID id;

    @Column(name = "full_name")
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", nullable = false, length = 20)
    private PaymentFrequency paymentFrequency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> paymentDetails = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
