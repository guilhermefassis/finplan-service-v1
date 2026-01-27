package io.finplan.domain.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.balance.ResponseBalanceCreditCardDTO;
import io.finplan.api.dto.balance.ResponseBalanceInvoiceDTO;
import io.finplan.domain.model.enums.StatusType;
import io.finplan.domain.repository.projection.CreditCardBalanceProjection;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper extends BaseMapper {

    public BalanceMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }


    public ResponseBalanceCreditCardDTO toResponseDTO(CreditCardBalanceProjection request) {
        return new ResponseBalanceCreditCardDTO(
                request.getCardId(),
                request.getCardName(),
                request.getBrand(),
                request.getCreditLimit(),
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
