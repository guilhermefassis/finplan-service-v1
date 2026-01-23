package io.finplan.domain.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.transactions.RequestCreditCardTransactionDTO;
import io.finplan.api.dto.transactions.ResponseCreditCardTransactionDTO;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardTransactions;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
        transaction.setAmount(request.amount());
        transaction.setInstallments(request.installments());
        transaction.setTotalInstallments(request.totalInstallments());
        transaction.setCurrentInstallment(request.currentInstallment());
        transaction.setTotalPurchaseAmount(sumTotalPurchaseAmount(request));

        return transaction;
    }

    private BigDecimal sumTotalPurchaseAmount(RequestCreditCardTransactionDTO request) {
        int totalInstallments = request.totalInstallments();
        return request.amount().multiply(BigDecimal.valueOf(totalInstallments));
    }

    public ResponseCreditCardTransactionDTO toResponseDTO(CreditCardTransactions request) {
        return new ResponseCreditCardTransactionDTO(
                request.getId(),
                request.getCreditCard().getId(),
                request.getCreditCardInvoice().getId(),
                request.getPurchaseDate(),
                request.getDescription(),
                request.getCategory(),
                request.getAmount(),
                request.isInstallments(),
                request.getTotalInstallments(),
                request.getCurrentInstallment(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

}
