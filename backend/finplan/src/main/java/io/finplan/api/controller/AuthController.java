package io.finplan.api.controller;

import io.finplan.api.dto.register.RegisterRequest;
import io.finplan.domain.service.RegisterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final RegisterService registerService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        UUID user_id = registerService.register(request);

        return Map.of(
                "user_id", user_id.toString()
        );
    }
}
