package io.finplan.api.controller;


import io.finplan.api.dto.transactions.RequestCreditCardTransactionDTO;
import io.finplan.api.dto.transactions.ResponseCreditCardTransactionDTO;
import io.finplan.domain.service.TransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/credit-cards/{cardId}/transaction")
@AllArgsConstructor
public class CreditCardTransactionController {

    private final TransactionsService transactionService;

    @GetMapping
    public List<ResponseCreditCardTransactionDTO> getAllCreditCardTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId
    ) {
        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);
        return transactionService.getTransactionByCreditCard(cardId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCreditCardTransactionDTO createTransaction(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody RequestCreditCardTransactionDTO request,
                                                              @PathVariable UUID cardId) {

        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);

        return transactionService.createCreditCardTransaction(request, userId, cardId);
    }


    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable UUID cardId, @PathVariable UUID groupId) {
        transactionService.deleteCreditCardTransaction(cardId, groupId);
    }
}
