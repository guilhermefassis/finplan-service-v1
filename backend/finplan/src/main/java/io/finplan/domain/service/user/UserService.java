package io.finplan.domain.service.user;

import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.api.dto.user.UserUpdateDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.repository.UserRepository;
import io.finplan.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserResponseDTO getUser(UUID user_id) {
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID user_id, UserUpdateDTO request) {
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())){
            throw new BusinessRuleException("Email already registered");
        }

        if(request.name() != null ) {
            user.setName(request.name());
        }

        if(request.email() != null) {
            user.setEmail(request.email());
        }

        if(request.paymentFrequency() != null){
            user.setPaymentFrequency(request.paymentFrequency());
        }

        if(request.paymentDetails() != null && !request.paymentDetails().isEmpty()) {
            user.setPaymentDetails(request.paymentDetails());
        }

        User updatedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponseDTO(updatedUser);
    }
}
