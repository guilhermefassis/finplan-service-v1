package io.finplan.api.controller;

import io.finplan.api.dto.invoice.ResponseInvoiceDTO;
import io.finplan.api.dto.invoice.UpdateInvoiceDTO;
import io.finplan.domain.service.InvoiceService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<ResponseInvoiceDTO> getInvoiceDetailsByDate(
            @PathVariable UUID cardId,
            @RequestParam(required = false) Integer referenceDate,
            @RequestParam(defaultValue = "false") boolean includeTransactions) {

        if(referenceDate != null) {
            return invoiceService.getInvoiceByReferenceDate(cardId, referenceDate, includeTransactions);
        }

        return invoiceService.getInvoicesByCreditCard(cardId);
    }

    @PostMapping("/{invoiceId}/process-status")
    public ResponseInvoiceDTO validateInvoiceStatus(
            @PathVariable UUID cardId,
            @PathVariable UUID invoiceId) {
        return invoiceService.validateAndChangeInvoiceStatus(invoiceId, cardId);
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseInvoiceDTO payInvoice(
            @PathVariable UUID cardId,
            @PathVariable UUID invoiceId,
            @RequestBody UpdateInvoiceDTO request) {
        return invoiceService.payInvoice(invoiceId, cardId, request);
    }

    @GetMapping("/reference-months")
    public List<Integer> getReferenceMonthsByCard(
            @PathVariable UUID cardId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return invoiceService.getAvailableReferenceMonthsByCard(userId, cardId);
    }
}
