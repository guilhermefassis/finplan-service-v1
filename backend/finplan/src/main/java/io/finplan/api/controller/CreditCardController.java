package io.finplan.api.controller;


import io.finplan.api.dto.creditcard.RequestCreditCardDTO;
import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.api.dto.creditcard.UpdateCreditCardDTO;
import io.finplan.domain.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/credit-cards")
@AllArgsConstructor
public class CreditCardController {
    private final CreditCardService creditCardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCreditCardDTO createCreditCard(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody RequestCreditCardDTO request) {
        String stringUserUuid = jwt.getSubject();
        UUID userUuid = UUID.fromString(stringUserUuid);
        return creditCardService.createCreditCard(request, userUuid);
    }

    @GetMapping
    public List<ResponseCreditCardDTO> getCreditCards(@AuthenticationPrincipal Jwt jwt) {
        String stringUserUuid = jwt.getSubject();
        UUID userUuid = UUID.fromString(stringUserUuid);
        return creditCardService.getCreditCards(userUuid);
    }

    @GetMapping("/disabled")
    public List<ResponseCreditCardDTO> getDisabledCreditCards(@AuthenticationPrincipal Jwt jwt) {
        String stringUserUuid = jwt.getSubject();
        UUID userUuid = UUID.fromString(stringUserUuid);
        return creditCardService.getDisabledCreditCards(userUuid);
    }

    @GetMapping("/{creditCardId}")
    public ResponseCreditCardDTO getCreditCard(@AuthenticationPrincipal Jwt jwt, @PathVariable String creditCardId) {
        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);
        UUID creditCardUuid = UUID.fromString(creditCardId);

        return creditCardService.getCreditCard(userId, creditCardUuid);
    }

    @DeleteMapping("/{creditCardId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableCreditCard(@AuthenticationPrincipal Jwt jwt, @PathVariable String creditCardId) {
        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);
        UUID creditCardUuid = UUID.fromString(creditCardId);

        creditCardService.disableCreditCard(userId, creditCardUuid);
    }

    @PostMapping("/{creditCardId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateCreditCard(@AuthenticationPrincipal Jwt jwt, @PathVariable String creditCardId) {
        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);
        UUID creditCardUuid = UUID.fromString(creditCardId);

        creditCardService.activateCreditCard(userId, creditCardUuid);
    }

    @PutMapping("/{creditCardId}")
    public ResponseCreditCardDTO updateCreditCard(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody UpdateCreditCardDTO request,
                                                  @PathVariable String creditCardId) {
        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);
        UUID creditCardUuid = UUID.fromString(creditCardId);

        return creditCardService.updateCreditCard(userId, creditCardUuid, request);
    }
}
