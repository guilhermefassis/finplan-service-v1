package io.finplan.api.dto.user;

import io.finplan.domain.model.enums.PaymentFrequency;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        PaymentFrequency paymentFrequency,
        Map<String, Object> paymentDetails,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
