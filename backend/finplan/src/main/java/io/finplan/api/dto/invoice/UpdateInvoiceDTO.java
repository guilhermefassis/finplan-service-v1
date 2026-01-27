package io.finplan.api.dto.invoice;

import java.time.LocalDate;

public record UpdateInvoiceDTO(
        //TODO validate this date
        LocalDate paymentDate
) {
}
