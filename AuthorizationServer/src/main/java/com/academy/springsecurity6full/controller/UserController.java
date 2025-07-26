package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.controller.dto.UserCreateDto;
import com.academy.springsecurity6full.controller.dto.UserResponseDto;
import com.academy.springsecurity6full.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus( HttpStatus.CREATED )
    public UserResponseDto createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto createdUser = userService.createUser(userCreateDto);
        return createdUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
