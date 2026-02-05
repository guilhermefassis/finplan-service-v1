package io.finplan.domain.service;


import io.finplan.common.utils.DateReferenceUtils;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.entity.CreditCardTransactions;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.InsufficientCreditLimitException;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.mapper.CreditCardTransactionMapper;
import io.finplan.domain.repository.CreditCardTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.finplan.api.dto.transactions.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionsService {

    private final CreditCardTransactionRepository transactionRepository;
    private final CreditCardService creditCardService;
    private final CreditLimitService limitService;
    private final CreditCardTransactionMapper transactionMapper;
    private final InvoiceService invoiceService;

    @Transactional
    public ResponseCreditCardTransactionDTO createCreditCardTransaction(
            RequestCreditCardTransactionDTO request,
            UUID userId,
            UUID creditCardId
    ) {
        CreditCard creditCard = creditCardService.validateAndReturnCreditCardId(creditCardId, userId);
        Integer firstInvoiceDate = getDateOfFirstInvoice(request.purchaseDate(), creditCard.getClosingDay());
        CreditCardTransactions transactionToReturn = null;
        CreditCardTransactions lastSaved = null;
        UUID groupId = UUID.randomUUID();

        if (!creditCard.isActive()) {
            throw new BusinessRuleException("Credit Card is deactivated!");
        }

        if(limitService.hasAvailableLimit(creditCardId, creditCard.getCreditLimit(), request.amount())) {
            throw new InsufficientCreditLimitException("The amount exceeds credit card limit!!");
        }
        Integer totalInstallments;
        if(!request.installments()) {
            totalInstallments = 1;
        } else {
            totalInstallments = request.totalInstallments();
        }
        for(int i = 0; i < totalInstallments; i++) {
            CreditCardTransactions transaction = transactionMapper.toEntity(request, creditCard);
            Integer invoiceDate = calculusNextMonth(firstInvoiceDate,i);
            CreditCardInvoice invoice = invoiceService.findOrCreateInvoice(creditCard, invoiceDate);

            transaction.setGroupId(groupId);
            int currentInstallment = i + 1;
            transaction.setCurrentInstallment(currentInstallment);
            transaction.setCreditCardInvoice(invoice);
            if(currentInstallment == request.currentInstallment()) {
                transactionToReturn = transactionRepository.saveAndFlush(transaction);
            } else {
                lastSaved = transactionRepository.save(transaction);
            }

            invoiceService.sumTransactionAmountInInvoice(invoice, transaction.getAmount());


        }

        return transactionMapper.toResponseDTO(transactionToReturn != null ? transactionToReturn: Objects.requireNonNull(lastSaved));
    }

    @Transactional
    public void deleteCreditCardTransaction(UUID cardId, UUID groupId) {
        List<CreditCardTransactions> transactions = transactionRepository.findByGroupIdAndCreditCard_Id(groupId, cardId);

        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("Any transaction as founded.");
        }

        for(CreditCardTransactions transaction: transactions) {
            if (transaction.getCreditCardInvoice() != null) {
                invoiceService.deductTransactionAmountInvoice(
                        transaction.getCreditCardInvoice().getId(),
                        transaction.getAmount()
                );
            }
        }
        transactionRepository.deleteByGroupId(groupId);
    }

    public List<ResponseCreditCardTransactionDTO> getTransactionByCreditCard(UUID creditCardId, UUID userId) {
        YearMonth yerMonth = YearMonth.now();
        Integer referenceMonth = DateReferenceUtils.toReference(yerMonth);
        CreditCard creditCard = creditCardService.validateAndReturnCreditCardId(creditCardId, userId);
        List<CreditCardTransactions> transactions = transactionRepository.findByCreditCardAndReferenceMonth(creditCard.getId(), referenceMonth);
        return transactionMapper.toListResponseDTO(transactions);
    }

    private Integer calculusNextMonth(Integer referenceDate, int monthsToSum) {
        YearMonth baseDate = DateReferenceUtils.fromReference(referenceDate);
        YearMonth transactionDate = baseDate.plusMonths(monthsToSum);

        return DateReferenceUtils.toReference(transactionDate);
    }

    private Integer getDateOfFirstInvoice(LocalDate purchaseDate, Integer closingDay) {
        YearMonth referenceMonth = YearMonth.from(purchaseDate);
        if(purchaseDate.getDayOfMonth() > closingDay) {
            referenceMonth = referenceMonth.plusMonths(1);
        }

        return referenceMonth.getYear() * 100 + referenceMonth.getMonthValue();
    }

}
