package io.finplan.service.user;


import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.entity.User;
import io.finplan.domain.repository.UserRepository;
import io.finplan.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        if(userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Email already registered!");
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

}
