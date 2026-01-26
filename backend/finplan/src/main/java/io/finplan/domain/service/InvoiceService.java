package io.finplan.domain.service;

import io.finplan.api.dto.invoice.ResponseInvoiceDTO;
import io.finplan.common.utils.DateReferenceUtils;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.mapper.InvoiceMapper;
import io.finplan.domain.model.enums.StatusType;
import io.finplan.domain.repository.CreditCardInvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final CreditCardInvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public CreditCardInvoice findOrCreateInvoice(CreditCard creditCard, Integer referenceMonth){
        return invoiceRepository
                .findByCreditCardIdAndReferenceMonth(creditCard.getId(), referenceMonth).orElseGet(() -> {
                    CreditCardInvoice newInvoice = new CreditCardInvoice();
                    YearMonth refMonth = DateReferenceUtils.fromReference(referenceMonth);
                    LocalDate closingDate = refMonth.atDay(
                            Math.min(creditCard.getClosingDay(), refMonth.lengthOfMonth())
                    );
                    YearMonth dueMonth = refMonth.plusMonths(1);
                    LocalDate dueDate = dueMonth.atDay(
                            Math.min(creditCard.getDueDay(), dueMonth.lengthOfMonth())
                    );


                   newInvoice.setCreditCard(creditCard);
                   newInvoice.setReferenceMonth(referenceMonth);
                   newInvoice.setClosingDate(closingDate);
                   newInvoice.setDueDate(dueDate);
                   newInvoice.setTotalAmount(BigDecimal.ZERO);

                   if(newInvoice.getDueDate().isBefore(LocalDate.now())) {
                       newInvoice.setStatus(StatusType.OVERDUE);
                   } else {
                       newInvoice.setStatus(StatusType.OPEN);
                   }

                   return invoiceRepository.save(newInvoice);

                });
    }

    public void deductTransactionAmountInvoice(UUID invoiceId, BigDecimal amount) {
        CreditCardInvoice invoice  = invoiceRepository.getReferenceById(invoiceId);
        invoice.setTotalAmount(invoice.getTotalAmount().subtract(amount));
        invoiceRepository.save(invoice);
    }

    public void sumTransactionAmountInInvoice(CreditCardInvoice invoice, BigDecimal amount) {
        invoice.setTotalAmount(invoice.getTotalAmount().add(amount));
        invoiceRepository.save(invoice);
    }

    public ResponseInvoiceDTO getInvoiceById(UUID cardId, UUID invoiceId, boolean includeTransactions) {
        CreditCardInvoice invoice;

        if(includeTransactions) {
            invoice = invoiceRepository.findByIdAndCreditCardIdWithTransactions(invoiceId, cardId).orElseThrow(
                    () -> new ResourceNotFoundException("Invoice not founded!")
            );
        } else {
            invoice = invoiceRepository.findByIdAndCreditCardId(invoiceId, cardId).orElseThrow(
                    () -> new ResourceNotFoundException("Invoice not founded!")
            );
        }

        return invoiceMapper.toResponse(invoice, includeTransactions);
    }

    public ResponseInvoiceDTO getInvoiceByReferenceDate(
            UUID cardId,
            Integer referenceDate,
            boolean includeTransactions
    ){
        CreditCardInvoice invoice;

        if(includeTransactions) {
            invoice = invoiceRepository.findByReferenceMonthAndCreditCardIdWithTransactions(referenceDate, cardId).orElseThrow(
                    () -> new ResourceNotFoundException("Invoice not founded!")
            );
        } else {
            invoice = invoiceRepository.findByReferenceMonthAndCreditCardId(referenceDate, cardId).orElseThrow(
                    () -> new ResourceNotFoundException("Invoice not founded!")
            );
        }

        return invoiceMapper.toResponse(invoice, includeTransactions);
    }
}
