package io.finplan.domain.mapper;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.model.enums.PaymentFrequency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("toEntity - Should convert the request Dto in a entity")
    void toEntity_ShouldConvertCorrectly() {
        // Arrange
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("bankAccount", "12345-6");
        paymentDetails.put("agency", "0001");

        UserRequestDTO dto = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                PaymentFrequency.MONTHLY,
                paymentDetails
        );


        User entity = userMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("João Silva", entity.getName());
        assertEquals("joao@email.com", entity.getEmail());
        assertEquals(PaymentFrequency.MONTHLY, entity.getPaymentFrequency());
        assertNotNull(entity.getPaymentDetails());
        assertEquals("12345-6", entity.getPaymentDetails().get("bankAccount"));
        assertEquals("0001", entity.getPaymentDetails().get("agency"));

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntity - Should deal with a empty entity")
    void toEntity_ShouldHandleEmptyPaymentDetails() {
        UserRequestDTO dto = new UserRequestDTO(
                "Maria Santos",
                "maria@email.com",
                PaymentFrequency.WEEKLY,
                new HashMap<>()
        );

        User entity = userMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Maria Santos", entity.getName());
        assertEquals("maria@email.com", entity.getEmail());
        assertEquals(PaymentFrequency.WEEKLY, entity.getPaymentFrequency());
        assertNotNull(entity.getPaymentDetails());
        assertTrue(entity.getPaymentDetails().isEmpty());
    }

    @Test
    @DisplayName("toResponseDTO - Should convert a user in response DTO")
    void toResponseDTO_ShouldConvertCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("bankAccount", "12345-6");
        paymentDetails.put("agency", "0001");

        User entity = User.builder()
                .id(userId)
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(paymentDetails)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserResponseDTO dto = userMapper.toResponseDTO(entity);

        assertNotNull(dto);
        assertEquals(userId, dto.id());
        assertEquals("João Silva", dto.name());
        assertEquals("joao@email.com", dto.email());
        assertEquals(PaymentFrequency.MONTHLY, dto.paymentFrequency());
        assertNotNull(dto.paymentDetails());
        assertEquals("12345-6", dto.paymentDetails().get("bankAccount"));
        assertEquals("0001", dto.paymentDetails().get("agency"));
        assertEquals(now, dto.createdAt());
        assertEquals(now, dto.updatedAt());
    }

    @Test
    @DisplayName("toResponseDTO - Should deal with empty payment details")
    void toResponseDTO_ShouldHandleEmptyPaymentDetails() {
        // Arrange
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        User entity = User.builder()
                .id(userId)
                .name("Maria Santos")
                .email("maria@email.com")
                .paymentFrequency(PaymentFrequency.WEEKLY)
                .paymentDetails(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserResponseDTO dto = userMapper.toResponseDTO(entity);

        assertNotNull(dto);
        assertEquals(userId, dto.id());
        assertEquals("Maria Santos", dto.name());
        assertEquals("maria@email.com", dto.email());
        assertEquals(PaymentFrequency.WEEKLY, dto.paymentFrequency());
        assertNotNull(dto.paymentDetails());
        assertTrue(dto.paymentDetails().isEmpty());
        assertEquals(now, dto.createdAt());
        assertEquals(now, dto.updatedAt());
    }

    @Test
    @DisplayName("toEntity e toResponseDTO ")
    void roundTrip_ShouldMaintainConsistency() {
        // Arrange
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("pix", "joao@email.com");

        UserRequestDTO requestDTO = new UserRequestDTO(
                "João Silva",
                "joao@email.com",
                PaymentFrequency.BIWEEKLY,
                paymentDetails
        );

        User entity = userMapper.toEntity(requestDTO);

        entity.setId(UUID.randomUUID());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        UserResponseDTO responseDTO = userMapper.toResponseDTO(entity);

        assertEquals(requestDTO.name(), responseDTO.name());
        assertEquals(requestDTO.email(), responseDTO.email());
        assertEquals(requestDTO.paymentFrequency(), responseDTO.paymentFrequency());
        assertEquals(requestDTO.paymentDetails().get("pix"),
                responseDTO.paymentDetails().get("pix"));
        assertNotNull(responseDTO.id());
        assertNotNull(responseDTO.createdAt());
        assertNotNull(responseDTO.updatedAt());
    }

    @Test
    @DisplayName("toResponseDTOList - Should convert User list to UserResponseDTO list")
    void toResponseDTOList_ShouldConvertList() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        User user1 = User.builder()
                .id(userId1)
                .name("João Silva")
                .email("joao@email.com")
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .paymentDetails(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        User user2 = User.builder()
                .id(userId2)
                .name("Maria Santos")
                .email("maria@email.com")
                .paymentFrequency(PaymentFrequency.WEEKLY)
                .paymentDetails(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<User> users = Arrays.asList(user1, user2);


        List<UserResponseDTO> dtos = userMapper.toResponseDTOList(users);


        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        assertEquals(userId1, dtos.get(0).id());
        assertEquals("João Silva", dtos.get(0).name());
        assertEquals("joao@email.com", dtos.get(0).email());
        assertEquals(PaymentFrequency.MONTHLY, dtos.get(0).paymentFrequency());

        assertEquals(userId2, dtos.get(1).id());
        assertEquals("Maria Santos", dtos.get(1).name());
        assertEquals("maria@email.com", dtos.get(1).email());
        assertEquals(PaymentFrequency.WEEKLY, dtos.get(1).paymentFrequency());
    }

    @Test
    @DisplayName("toResponseDTOList - Should return a empty list when received a empty list")
    void toResponseDTOList_ShouldReturnEmptyList_WhenInputIsEmpty() {
        // Arrange
        List<User> emptyList = Collections.emptyList();

        // Act
        List<UserResponseDTO> result = userMapper.toResponseDTOList(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}