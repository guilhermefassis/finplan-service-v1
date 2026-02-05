package io.finplan.domain.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.creditcard.RequestCreditCardDTO;
import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreditCardMapper extends BaseMapper {
    public CreditCardMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public CreditCard toEntity(RequestCreditCardDTO dto, User user) {
        CreditCard creditCard = new CreditCard();
        creditCard.setName(dto.name());
        creditCard.setBrand(dto.brand());
        creditCard.setUser(user);
        creditCard.setClosingDay(dto.closingDay());
        creditCard.setCreditLimit(dto.creditLimit());
        creditCard.setDueDay(dto.dueDay());

        return creditCard;
    }

    public ResponseCreditCardDTO toResponseDTO(CreditCard creditCard) {
        return new ResponseCreditCardDTO(
          creditCard.getId(),
          creditCard.getUser().getId(),
          creditCard.getName(),
          creditCard.getBrand(),
          creditCard.getClosingDay(),
          creditCard.getDueDay(),
          creditCard.getCreditLimit(),
          creditCard.isActive(),
          creditCard.getCreatedAt(),
          creditCard.getUpdatedAt()
        );
    }

    public List<ResponseCreditCardDTO> toListResponseDTO(List<CreditCard> creditCards) {
        return creditCards.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
