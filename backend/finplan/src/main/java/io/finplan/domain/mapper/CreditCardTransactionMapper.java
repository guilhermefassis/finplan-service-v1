package io.finplan.domain.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.api.dto.transactions.RequestCreditCardTransactionDTO;
import io.finplan.api.dto.transactions.ResponseCreditCardTransactionDTO;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardTransactions;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CreditCardTransactionMapper extends BaseMapper {
    public CreditCardTransactionMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }


    public CreditCardTransactions toEntity(RequestCreditCardTransactionDTO request, CreditCard creditCard) {
        CreditCardTransactions transaction = new CreditCardTransactions();
        transaction.setCreditCard(creditCard);
        transaction.setPurchaseDate(request.purchaseDate());
        transaction.setDescription(request.description());
        transaction.setCategory(request.category());
        transaction.setTotalPurchaseAmount(request.amount());
        transaction.setInstallments(request.installments());
        if(!request.installments()) {
            transaction.setTotalInstallments(1);
            transaction.setCurrentInstallment(1);
            transaction.setAmount(request.amount());
        } else {
            transaction.setTotalInstallments(request.totalInstallments());
            transaction.setCurrentInstallment(request.currentInstallment());
            transaction.setAmount(getInstallmentAmount(request));
        }

        return transaction;
    }

    private BigDecimal getInstallmentAmount(RequestCreditCardTransactionDTO request) {
        int totalInstallments = request.totalInstallments();
        return request.amount().divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_DOWN);
    }

    public ResponseCreditCardTransactionDTO toResponseDTO(CreditCardTransactions request) {
        return new ResponseCreditCardTransactionDTO(
                request.getId(),
                request.getCreditCard().getId(),
                request.getCreditCardInvoice().getId(),
                request.getGroupId(),
                request.getPurchaseDate(),
                request.getDescription(),
                request.getCategory(),
                request.getAmount(),
                request.isInstallments(),
                request.getTotalInstallments(),
                request.getCurrentInstallment(),
                request.getTotalPurchaseAmount(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    public List<ResponseCreditCardTransactionDTO> toListResponseDTO(List<CreditCardTransactions> transactions) {
        return transactions.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
