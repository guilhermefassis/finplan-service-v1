package io.finplan.domain.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.balance.ResponseBalanceCreditCardDTO;
import io.finplan.api.dto.balance.ResponseBalanceInvoiceDTO;
import io.finplan.domain.model.enums.StatusType;
import io.finplan.domain.repository.projection.CreditCardBalanceProjection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BalanceMapper extends BaseMapper {

    public BalanceMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }


    public ResponseBalanceCreditCardDTO toResponseDTO(CreditCardBalanceProjection request, BigDecimal availableLimit) {
        if (availableLimit.compareTo(BigDecimal.ZERO) < 0) {
            availableLimit = availableLimit.multiply(BigDecimal.valueOf(-1));
        }
        return new ResponseBalanceCreditCardDTO(
                request.getCardId(),
                request.getCardName(),
                request.getBrand(),
                request.getCreditLimit(),
                request.getCreditLimit().subtract(availableLimit),
                availableLimit,
                this.toInvoiceDTO(request)
        );
    }


    private ResponseBalanceInvoiceDTO toInvoiceDTO(CreditCardBalanceProjection request) {
        if(request.getInvoiceId() == null) {
            return null;
        }
        return new ResponseBalanceInvoiceDTO(
                request.getInvoiceId(),
                request.getReferenceMonth(),
                request.getClosingDate(),
                request.getDueDate(),
                request.getTotalAmount(),
                StatusType.valueOf(request.getStatus()),
                request.getPaymentDate()
        );
    }
}
