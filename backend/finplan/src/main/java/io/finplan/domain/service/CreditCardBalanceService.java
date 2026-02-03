package io.finplan.domain.service;

import io.finplan.api.dto.balance.ResponseBalanceCreditCardDTO;
import io.finplan.api.dto.balance.ResponseBalanceDTO;
import io.finplan.domain.mapper.BalanceMapper;
import io.finplan.domain.repository.CreditCardRepository;
import io.finplan.domain.repository.projection.CreditCardBalanceProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreditCardBalanceService {

    private final CreditCardRepository creditCardRepository;
    private final BalanceMapper balanceMapper;
    private final CreditLimitService limitService;

    public ResponseBalanceDTO getMonthlyBalance(UUID userId, Integer referenceMonth) {
        List<CreditCardBalanceProjection> results =
                creditCardRepository.findMonthlyBalanceByUserAndMonth(userId, referenceMonth);

        BigDecimal totalMonthAmount = BigDecimal.ZERO;
        List<ResponseBalanceCreditCardDTO> creditCards = new ArrayList<>();

        for(CreditCardBalanceProjection balance : results) {
            BigDecimal usageLimit = limitService.calculateAvailableLimit(balance.getCardId(), balance.getCreditLimit());
            creditCards.add(balanceMapper.toResponseDTO(balance, usageLimit));
            if(balance.getTotalAmount() != null) {
                totalMonthAmount = totalMonthAmount.add(balance.getTotalAmount());
            }
        }

        return new ResponseBalanceDTO(
                referenceMonth,
                totalMonthAmount,
                creditCards
        );
    }
}
