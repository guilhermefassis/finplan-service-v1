package io.finplan.api.controller;

import io.finplan.api.dto.user.UserRequestDTO;
import io.finplan.api.dto.user.UserResponseDTO;
import io.finplan.api.dto.user.UserUpdateDTO;
import io.finplan.domain.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDTO getUser(@AuthenticationPrincipal Jwt jwt) {
        String stringUuid = jwt.getSubject();
        UUID uuid = UUID.fromString(stringUuid);
        return userService.getUser(uuid);
    }

    @PutMapping
    public UserResponseDTO updateUser(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserUpdateDTO requestDTO) {
        String stringUuid = jwt.getSubject();
        UUID uuid = UUID.fromString(stringUuid);
        return userService.updateUser(uuid, requestDTO);
    }

}
