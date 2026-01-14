package io.finplan.domain.service;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.*;

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
    @DisplayName("createUser - Should create a new User")
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
    @DisplayName("createUser - Should throws a BusinessRuleException")
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


    @Test
    @DisplayName("getUsers - should return an empty page when no users are registered")
    void getUsers_ShouldReturnEmptyPage_WhenNoUsersExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = Page.empty(pageable);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<UserResponseDTO> result = userService.getUsers(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(userRepository).findAll(pageable);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("getUser - should return user when found")
    void getUser_ShouldReturnUser_WhenFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        UserResponseDTO expectedDto = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPaymentFrequency(),
                user.getPaymentDetails(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(eq(user))).thenReturn(expectedDto);

        // Act
        UserResponseDTO result = userService.getUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("João Silva", result.name());
        assertEquals("joao@email.com", result.email());

        verify(userRepository).findById(userId);
        verify(userMapper).toResponseDTO(user);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getUser - should throw ResourceNotFoundException when user not found")
    void getUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUser(userId)
        );

        assertEquals("User not found", ex.getMessage());

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("deleteUser - should delete user when found")
    void deleteUser_ShouldDeleteUser_WhenFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("deleteUser - should throw ResourceNotFoundException when user not found")
    void deleteUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("User not fount", ex.getMessage()); // Mantive o typo do seu código

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }
}