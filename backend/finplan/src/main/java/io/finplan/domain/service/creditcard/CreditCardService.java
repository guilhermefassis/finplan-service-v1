package io.finplan.domain.service.creditcard;


import io.finplan.api.dto.creditcard.RequestCreditCardDTO;
import io.finplan.api.dto.creditcard.ResponseCreditCardDTO;
import io.finplan.domain.entity.CreditCard;
import io.finplan.domain.entity.User;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.mapper.CreditCardMapper;
import io.finplan.domain.repository.CreditCardRepository;
import io.finplan.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardMapper creditCardMapper;
    private final UserRepository userRepository;

    @Transactional
    public ResponseCreditCardDTO createCreditCard(RequestCreditCardDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CreditCard creditCard = creditCardMapper.toEntity(request, user);

        creditCardRepository.saveAndFlush(creditCard);

        return creditCardMapper.toResponseDTO(creditCard);
    }

    public List<ResponseCreditCardDTO> getCreditCards(UUID userId) {
        List<CreditCard> creditCards = creditCardRepository.findByUserId(userId);
        return creditCardMapper.toListResponseDTO(creditCards);
    }

    public ResponseCreditCardDTO getCreditCard(UUID userId, UUID creditCardId) {

        CreditCard creditCard = creditCardRepository.findByIdAndUserId(creditCardId, userId)
                .orElseThrow(() -> new BusinessRuleException("Credit Card not exists!"));

        return creditCardMapper.toResponseDTO(creditCard);
    }
}
