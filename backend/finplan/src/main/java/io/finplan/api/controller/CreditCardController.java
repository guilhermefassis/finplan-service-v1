package io.finplan.api.controller;


import io.finplan.api.dto.creditcard.RequestCreditCardDTO;
import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.domain.service.creditcard.CreditCardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/credit-cards")
@AllArgsConstructor
public class CreditCardController {
    private final CreditCardService creditCardService;

    @PostMapping
    public ResponseCreditCardDTO createCreditCard(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody RequestCreditCardDTO request) {
        String stringUserUuid = jwt.getSubject();
        UUID userUuid = UUID.fromString(stringUserUuid);
        return creditCardService.createCreditCard(request, userUuid);
    }

    @PostMapping("/test")
    public String test(HttpServletRequest req) throws Exception {
        System.out.println("LENGTH: " + req.getContentLength());
        return new String(req.getInputStream().readAllBytes());
    }
}
