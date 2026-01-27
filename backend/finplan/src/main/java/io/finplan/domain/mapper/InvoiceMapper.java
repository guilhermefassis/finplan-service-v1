package io.finplan.domain.mapper;

import io.finplan.api.dto.invoice.ResponseInvoiceDTO;
import io.finplan.api.dto.transactions.ResponseCreditCardTransactionDTO;
import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.entity.CreditCardTransactions;

import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class InvoiceMapper {

    public ResponseInvoiceDTO toResponse(CreditCardInvoice invoice, boolean includeTransactions) {
        return new ResponseInvoiceDTO(
                invoice.getId(),
                invoice.getCreditCard().getId(),
                invoice.getReferenceMonth(),
                invoice.getClosingDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getPaymentDate(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                includeTransactions && invoice.getTransactions() != null
                        ? invoice.getTransactions().stream()
                        .map(this::transactionToResponse)
                        .toList()
                        : null
        );
    }

    private ResponseCreditCardTransactionDTO transactionToResponse(CreditCardTransactions transaction) {
        return new ResponseCreditCardTransactionDTO(
                transaction.getId(),
                transaction.getCreditCard().getId(),
                transaction.getCreditCardInvoice() != null
                        ? transaction.getCreditCardInvoice().getId()
                        : null,
                transaction.getGroupId(),
                transaction.getPurchaseDate(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.isInstallments(),
                transaction.getTotalInstallments(),
                transaction.getCurrentInstallment(),
                transaction.getTotalPurchaseAmount(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public List<ResponseInvoiceDTO> toListResponseDTO(List<CreditCardInvoice> invoices, boolean includeTransactions) {
        return invoices
                .stream()
                .map(invoice -> toResponse(invoice, includeTransactions))
                .toList();
    }
}