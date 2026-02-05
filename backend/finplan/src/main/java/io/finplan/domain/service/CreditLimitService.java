package io.finplan.domain.service;

import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.model.enums.StatusType;
import io.finplan.domain.repository.CreditCardInvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreditLimitService {
    private final CreditCardInvoiceRepository invoiceRepository;

    public BigDecimal calculateAvailableLimit(UUID creditCardId, BigDecimal creditLimit) {
        List<CreditCardInvoice> unpaidInvoices = invoiceRepository
                .findByCreditCardIdAndStatusIn(
                        creditCardId,
                        List.of(StatusType.OPEN, StatusType.CLOSED, StatusType.OVERDUE)
                );
        BigDecimal usedLimit = unpaidInvoices.stream()
                .map(CreditCardInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return creditLimit.subtract(usedLimit);
    }

    public boolean hasAvailableLimit(UUID creditCardId, BigDecimal creditLimit, BigDecimal transactionAmount) {
        BigDecimal availableLimit = calculateAvailableLimit(creditCardId, creditLimit);
        return availableLimit.compareTo(transactionAmount) <= 0;
    }

    public BigDecimal calculateUsedLimit(UUID creditCardId) {
        List<CreditCardInvoice> unpaidInvoices = invoiceRepository
                .findByCreditCardIdAndStatusIn(
                        creditCardId,
                        List.of(StatusType.OPEN, StatusType.CLOSED, StatusType.OVERDUE)
                );

        return unpaidInvoices.stream()
                .map(CreditCardInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
