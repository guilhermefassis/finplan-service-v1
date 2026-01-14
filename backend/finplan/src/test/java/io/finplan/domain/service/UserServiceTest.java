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
    @DisplayName("getUsers - Should return a list with users")
    void getUsers_ShouldReturnUserList() {

        User user1 = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Santos")
                .email("maria@email.com")
                .paymentFrequency(PaymentFrequency.WEEKLY)
                .paymentDetails(new HashMap<>())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        List<User> users = Arrays.asList(user1, user2);

        UserResponseDTO dto1 = new UserResponseDTO(
                user1.getId(),
                user1.getName(),
                user1.getEmail(),
                user1.getPaymentFrequency(),
                user1.getPaymentDetails(),
                user1.getCreatedAt(),
                user1.getUpdatedAt()
        );

        UserResponseDTO dto2 = new UserResponseDTO(
                user2.getId(),
                user2.getName(),
                user2.getEmail(),
                user2.getPaymentFrequency(),
                user2.getPaymentDetails(),
                user2.getCreatedAt(),
                user2.getUpdatedAt()
        );

        List<UserResponseDTO> expectedDtos = Arrays.asList(dto1, dto2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseDTOList(eq(users))).thenReturn(expectedDtos);

        // Act
        List<UserResponseDTO> result = userService.getUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("joao@email.com", result.get(0).email());
        assertEquals("maria@email.com", result.get(1).email());

        verify(userRepository).findAll();
        verify(userMapper).toResponseDTOList(users);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("getUsers - Should return a empty list when don't have users registered")
    void getUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        List<User> emptyList = Collections.emptyList();
        List<UserResponseDTO> emptyDtoList = Collections.emptyList();

        when(userRepository.findAll()).thenReturn(emptyList);
        when(userMapper.toResponseDTOList(eq(emptyList))).thenReturn(emptyDtoList);

        // Act
        List<UserResponseDTO> result = userService.getUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
        verify(userMapper).toResponseDTOList(emptyList);
        verifyNoMoreInteractions(userRepository, userMapper);
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
}