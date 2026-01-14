package io.finplan.domain.service;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.mapper.UserMapper;
import io.finplan.domain.model.enums.PaymentFrequency;
import io.finplan.domain.repository.UserRepository;
import io.finplan.domain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("createUser - Must create a new User")
    void createUser_Success() {
        // Arrange
        UserRequestDTO request = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                PaymentFrequency.MONTHLY,
                new HashMap<>()
        );

        User userToSave = User.builder()
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        UserResponseDTO responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPaymentFrequency(),
                savedUser.getPaymentDetails(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
        );

        when(userRepository.existsByEmail(eq(request.email()))).thenReturn(false);
        when(userMapper.toEntity(eq(request))).thenReturn(userToSave);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDTO(eq(savedUser))).thenReturn(responseDTO);

        // Act
        UserResponseDTO result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.email(), result.email());
        assertEquals(request.name(), result.name());
        assertNotNull(result.id());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());

        verify(userRepository).existsByEmail(request.email());
        verify(userMapper).toEntity(request);
        verify(userRepository).saveAndFlush(userToSave);
        verify(userMapper).toResponseDTO(savedUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("createUser - Must throws a BusinessRuleException")
    void createUser_ShouldThrowBusinessRuleException_WhenEmailAlreadyExists() {
        // Arrange
        UserRequestDTO request = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                PaymentFrequency.MONTHLY,
                new HashMap<>()
        );

        when(userRepository.existsByEmail(eq(request.email()))).thenReturn(true);

        // Act + Assert
        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Email already registered", ex.getMessage());

        verify(userRepository).existsByEmail(request.email());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper); // não deve mapear nem salvar
    }
}