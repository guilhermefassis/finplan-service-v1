package io.finplan.api.dto.user;

import io.finplan.domain.model.enums.PaymentFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UserRequestDTO (
    @NotBlank(message = "Name is required!")
    String name,
    @NotBlank(message = "The Email is required!")
    @Email
    String email,
    @NotNull(message = "Payment frequency is required!")
    PaymentFrequency paymentFrequency,
    Map<String, Object> paymentDetails
){}