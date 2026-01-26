package io.finplan.api.controller;

import io.finplan.api.dto.invoice.ResponseInvoiceDTO;
import io.finplan.domain.service.InvoiceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/credit-cards/{cardId}/invoices")
@AllArgsConstructor
public class CreditCardInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{invoiceId}")
    public ResponseInvoiceDTO getInvoiceDetailsById(
            @PathVariable UUID cardId,
            @PathVariable UUID invoiceId,
            @RequestParam(defaultValue = "false") boolean includeTransactions) {

       return  invoiceService.getInvoiceById(
                cardId,
                invoiceId,
                includeTransactions
        );
    }

    @GetMapping
    public ResponseInvoiceDTO getInvoiceDetailsByDate(
            @PathVariable UUID cardId,
            @RequestParam Integer referenceDate,
            @RequestParam(defaultValue = "false") boolean includeTransactions) {

        return invoiceService.getInvoiceByReferenceDate(cardId, referenceDate, includeTransactions);
    }
}
