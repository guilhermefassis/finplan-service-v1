package io.finplan.api.dto.user;

import io.finplan.domain.model.enums.PaymentFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UserUpdateDTO(
        @Size(min = 3, max = 100)
        String name,
        @Email
        String email,
        PaymentFrequency paymentFrequency,
        Map<String, Object> paymentDetails
) {}
