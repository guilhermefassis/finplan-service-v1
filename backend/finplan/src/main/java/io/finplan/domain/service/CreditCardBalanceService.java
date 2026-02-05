package io.finplan.domain.service;

import io.finplan.api.dto.balance.CategoryExpenseDetailDTO;
import io.finplan.api.dto.balance.ExpensesByCategoryResponse;
import io.finplan.api.dto.balance.ResponseBalanceCreditCardDTO;
import io.finplan.api.dto.balance.ResponseBalanceDTO;
import io.finplan.domain.mapper.BalanceMapper;
import io.finplan.domain.model.enums.CreditCardTransactionCategory;
import io.finplan.domain.model.enums.ExpenseCategory;
import io.finplan.domain.repository.CreditCardRepository;
import io.finplan.domain.repository.CreditCardTransactionRepository;
import io.finplan.domain.repository.projection.CategoryExpenseProjection;
import io.finplan.domain.repository.projection.CategoryTotalProjection;
import io.finplan.domain.repository.projection.CreditCardBalanceProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CreditCardBalanceService {

    private final CreditCardRepository creditCardRepository;
    private final BalanceMapper balanceMapper;
    private final CreditLimitService limitService;
    private final CreditCardTransactionRepository transactionRepository;

    public ResponseBalanceDTO getMonthlyBalance(UUID userId, Integer referenceMonth) {
        List<CreditCardBalanceProjection> results =
                creditCardRepository.findMonthlyBalanceByUserAndMonth(userId, referenceMonth);

        BigDecimal totalMonthAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ResponseBalanceCreditCardDTO> creditCards = new ArrayList<>();

        for(CreditCardBalanceProjection balance : results) {
            BigDecimal availableLimit = limitService.calculateAvailableLimit(balance.getCardId(), balance.getCreditLimit());
            creditCards.add(balanceMapper.toResponseDTO(balance, availableLimit));
            BigDecimal usageLimit = balance.getCreditLimit().subtract(availableLimit);
            totalAmount = totalAmount.add(usageLimit);

            if(balance.getTotalAmount() != null) {
                totalMonthAmount = totalMonthAmount.add(balance.getTotalAmount());
            }
        }

        return new ResponseBalanceDTO(
                referenceMonth,
                totalMonthAmount,
                totalAmount,
                creditCards
        );
    }

    public ExpensesByCategoryResponse getExpensesByCategory(UUID userId, Integer referenceMonth) {
        YearMonth yearMonth = parseReferenceMonth(referenceMonth);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<CategoryExpenseProjection> monthlyExpenses =
                transactionRepository.findMonthlyExpensesByCategory(userId, referenceMonth);

        List<CategoryTotalProjection> totalExpenses =
                transactionRepository.findTotalAccumulatedExpensesByCategory(userId, endDate);

        Map<String, BigDecimal> accumulatedMap = totalExpenses.stream()
                .collect(Collectors.toMap(
                        CategoryTotalProjection::getCategory,
                        CategoryTotalProjection::getTotalAmount
                ));

        BigDecimal totalMonthlyAmount = monthlyExpenses.stream()
                .map(CategoryExpenseProjection::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryExpenseDetailDTO> categories = monthlyExpenses.stream()
                .map(expense -> {
                    String categoryStr = expense.getCategory();
                    CreditCardTransactionCategory category = CreditCardTransactionCategory.valueOf(categoryStr);
                    BigDecimal monthlyAmount = expense.getMonthlyAmount();
                    BigDecimal accumulatedAmount = accumulatedMap.getOrDefault(categoryStr, BigDecimal.ZERO);

                    Double percentage = totalMonthlyAmount.compareTo(BigDecimal.ZERO) > 0
                            ? monthlyAmount.divide(totalMonthlyAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return new CategoryExpenseDetailDTO(
                            category,
                            category.toString(),
                            monthlyAmount,
                            accumulatedAmount,
                            percentage,
                            expense.getTransactionCount().intValue()
                    );
                })
                .collect(Collectors.toList());

        return new ExpensesByCategoryResponse(
                referenceMonth,
                "BRL",
                totalMonthlyAmount,
                categories
        );
    }

    private YearMonth parseReferenceMonth(Integer referenceMonth) {
        int year = referenceMonth / 100;
        int month = referenceMonth % 100;
        return YearMonth.of(year, month);
    }
}

