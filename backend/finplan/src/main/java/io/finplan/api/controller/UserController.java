package io.finplan.api.controller;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        return userService.createUser(requestDTO);
    }

    @GetMapping
    public List<UserResponseDTO> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{user_id}")
    public UserResponseDTO getUser(@PathVariable UUID user_id) {
        return userService.getUser(user_id);
    }

}
