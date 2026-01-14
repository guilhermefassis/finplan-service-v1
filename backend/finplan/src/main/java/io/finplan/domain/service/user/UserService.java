package io.finplan.domain.service.user;


import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.exception.BusinessRuleException;
import io.finplan.domain.exception.ResourceNotFoundException;
import io.finplan.domain.repository.UserRepository;
import io.finplan.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        if(userRepository.existsByEmail(request.email())){
            throw new BusinessRuleException("Email already registered");
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponseDTO(savedUser);
    }

    public Page<UserResponseDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    public UserResponseDTO getUser(UUID user_id) {
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponseDTO(user);
    }

}
