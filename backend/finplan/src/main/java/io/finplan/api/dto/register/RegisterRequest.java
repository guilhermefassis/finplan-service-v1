package io.finplan.api.dto.register;

public record RegisterRequest(
        String email,
        String password,
        String fullName
) {
}
