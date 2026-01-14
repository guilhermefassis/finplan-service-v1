package io.finplan.api.controller;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.domain.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public Page<UserResponseDTO> getUsers(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @GetMapping("/{user_id}")
    public UserResponseDTO getUser(@PathVariable UUID user_id) {
        return userService.getUser(user_id);
    }

    @DeleteMapping("/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID user_id) { userService.deleteUser(user_id);}

}
