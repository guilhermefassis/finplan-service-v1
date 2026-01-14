package io.finplan.domain.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper extends BaseMapper {

    public UserMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public User toEntity (UserRequestDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPaymentFrequency(dto.paymentFrequency());
        user.setPaymentDetails(dto.paymentDetails());

        return user;
    }

    public UserResponseDTO toResponseDTO (User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPaymentFrequency(),
                user.getPaymentDetails(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public List<UserResponseDTO> toResponseDTOList(List<User> users) {
        return users.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
