package io.finplan.api.controller;

import io.finplan.api.dto.balance.ExpensesByCategoryResponse;
import io.finplan.api.dto.balance.ResponseBalanceDTO;
import io.finplan.domain.service.CreditCardBalanceService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/balances")
@AllArgsConstructor
public class BalanceController {

    private final CreditCardBalanceService cardBalanceService;


    @GetMapping("cards")
    public ResponseBalanceDTO getMonthlyBalance(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam Integer referenceDate) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return cardBalanceService.getMonthlyBalance(userId, referenceDate);
    }

    @GetMapping("/analytics/expenses/by-category")
    public ExpensesByCategoryResponse getExpensesByCategory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer referenceMonth
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return cardBalanceService.getExpensesByCategory(userId, referenceMonth);
    }


}
