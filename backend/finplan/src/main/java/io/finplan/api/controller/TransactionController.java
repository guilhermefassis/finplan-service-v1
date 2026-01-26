package io.finplan.api.controller;

import io.finplan.api.dto.transactions.*;

import io.finplan.domain.service.CreditCardTransactionsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transaction")
@AllArgsConstructor
public class TransactionController {
    private final CreditCardTransactionsService transactionService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCreditCardTransactionDTO createTransaction(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody RequestCreditCardTransactionDTO request) {

        String stringUserId = jwt.getSubject();
        UUID userId = UUID.fromString(stringUserId);

        return transactionService.createCreditCardTransaction(request, userId);
    }


    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable String groupId) {
        UUID groupUuid = UUID.fromString(groupId);
        transactionService.deleteCreditCardTransaction(groupUuid);
    }
}
