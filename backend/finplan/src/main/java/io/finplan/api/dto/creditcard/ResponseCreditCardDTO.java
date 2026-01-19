package io.finplan.api.dto.creditcard;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ResponseCreditCardDTO(
        UUID id,
        UUID userId,
        String name,
        String brand,
        Integer closingDay,
        Integer dueDay,
        BigDecimal creditLimit,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
