package io.finplan.domain.service;


import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.common.utils.DateReferenceUtils;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.entity.CreditCardTransactions;
import io.finplan.domain.mapper.CreditCardTransactionMapper;
import io.finplan.domain.repository.CreditCardTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.finplan.api.dto.transactions.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreditCardTransactionsService {

    private final CreditCardTransactionRepository transactionRepository;
    private final CreditCardService creditCardService;
    private final CreditCardTransactionMapper transactionMapper;
    private final InvoiceService invoiceService;

    @Transactional
    public ResponseCreditCardTransactionDTO createCreditCardTransaction(RequestCreditCardTransactionDTO request, UUID userId) {
        CreditCard creditCard = creditCardService.validateAndReturnCreditCardId(request.creditCardId(), userId);
        Integer firstInvoiceDate = getDateOfFirstInvoice(request.purchaseDate(), creditCard.getClosingDay());
        CreditCardTransactions transactionToReturn = null;
        CreditCardTransactions lastSaved = null;

        for(int i = 0; i < request.totalInstallments(); i++) {
            CreditCardTransactions transaction = transactionMapper.toEntity(request, creditCard);
            Integer invoiceDate = calculusNextMonth(firstInvoiceDate,i);
            CreditCardInvoice invoice = invoiceService.findOrCreateInvoice(creditCard, invoiceDate);

            int currentInstallment = i + 1;
            transaction.setCurrentInstallment(currentInstallment);
            transaction.setCreditCardInvoice(invoice);
            if(currentInstallment == request.currentInstallment()) {
                transactionToReturn = transactionRepository.saveAndFlush(transaction);;
            } else {
                lastSaved = transactionRepository.save(transaction);;
            }

            invoiceService.sumTransactionAmountInInvoice(invoice, transaction.getAmount());


        }

        return transactionMapper.toResponseDTO(transactionToReturn != null ? transactionToReturn: Objects.requireNonNull(lastSaved));
    }

    private Integer calculusNextMonth(Integer referenceDate, int monthsToSum) {
        YearMonth baseDate = DateReferenceUtils.fromReference(referenceDate);
        YearMonth transactionDate = baseDate.plusMonths(monthsToSum);

        return (transactionDate.getYear() * 100) + transactionDate.getMonthValue();
    }

    private Integer getDateOfFirstInvoice(LocalDate purchaseDate, Integer closingDay) {
        YearMonth referenceMonth = YearMonth.from(purchaseDate);
        if(purchaseDate.getDayOfMonth() > closingDay) {
            referenceMonth = referenceMonth.plusMonths(1);
        }

        return referenceMonth.getYear() * 100 + referenceMonth.getMonthValue();
    }

}
