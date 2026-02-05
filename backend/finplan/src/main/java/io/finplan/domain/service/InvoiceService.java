package io.finplan.domain.service;

import io.finplan.api.dto.invoice.ResponseInvoiceDTO;
import io.finplan.api.dto.invoice.UpdateInvoiceDTO;
import io.finplan.common.utils.DateReferenceUtils;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.CreditCardInvoice;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.mapper.InvoiceMapper;
import io.finplan.domain.model.enums.StatusType;
import io.finplan.domain.repository.CreditCardInvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
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
                    closingDate = nextBusinessDay(closingDate);

                    YearMonth dueMonth;

                    if (creditCard.getDueDay() > creditCard.getClosingDay()) {
                    dueMonth = refMonth;
                    } else {
                    dueMonth = refMonth.plusMonths(1);
                    }

                    LocalDate dueDate = dueMonth.atDay(
                    Math.min(creditCard.getDueDay(), dueMonth.lengthOfMonth())
                    );
                    dueDate = nextBusinessDay(dueDate);

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

    public List<ResponseInvoiceDTO> getInvoiceByReferenceDate(
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

        return List.of(invoiceMapper.toResponse(invoice, includeTransactions));
    }

    public List<ResponseInvoiceDTO> getInvoicesByCreditCard(UUID cardId) {
        List<CreditCardInvoice> invoices = invoiceRepository.findByCreditCardId(cardId);
        return invoiceMapper.toListResponseDTO(invoices, false);
    }

    public ResponseInvoiceDTO validateAndChangeInvoiceStatus(UUID invoiceId, UUID cardId) {
        CreditCardInvoice invoice = invoiceRepository.findByIdAndCreditCardId(invoiceId, cardId).orElseThrow(
                () -> new ResourceNotFoundException("Invoice Not Found")
        );

        if (invoice.getStatus() == StatusType.PAID) {
            return invoiceMapper.toResponse(invoice, false);
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(invoice.getDueDate())) {
            invoice.setStatus(StatusType.OVERDUE);
            CreditCardInvoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
            return invoiceMapper.toResponse(savedInvoice, false);
        }
        if (today.isAfter(invoice.getClosingDate())) {
            invoice.setStatus(StatusType.CLOSED);
            CreditCardInvoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
            return invoiceMapper.toResponse(savedInvoice, false);
        }

        throw new BusinessRuleException(
                String.format("Invoice is still open. Closing date: %s, Due date: %s",
                        invoice.getClosingDate(), invoice.getDueDate())
        );

    }

    public ResponseInvoiceDTO payInvoice(UUID invoiceId, UUID cardId, UpdateInvoiceDTO request) {
        CreditCardInvoice invoice = invoiceRepository.findByIdAndCreditCardId(invoiceId, cardId).orElseThrow(
                () -> new ResourceNotFoundException("Invoice Not Found")
        );

        if (invoice.getStatus() == StatusType.PAID) {
            return invoiceMapper.toResponse(invoice, false);
        }

        invoice.setPaymentDate(request.paymentDate());
        invoice.setStatus(StatusType.PAID);

        CreditCardInvoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
        return invoiceMapper.toResponse(savedInvoice, false);
    }

    public List<Integer> getAvailableReferenceMonths(UUID userId) {
        return invoiceRepository.findDistinctReferenceMonthsByUserId(userId);
    }

    public List<Integer> getAvailableReferenceMonthsByCard(UUID userId, UUID cardId) {
        return invoiceRepository.findDistinctReferenceMonthsByUserIdAndCardId(userId, cardId);
    }

    private boolean isBusinessDay(LocalDate date) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    private LocalDate nextBusinessDay(LocalDate date) {
        LocalDate next = date;
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }
}
