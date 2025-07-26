package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.controller.dto.RegisteredClientCreateDto;
import com.academy.springsecurity6full.controller.dto.RegisteredClientResponseDto;
import com.academy.springsecurity6full.service.RegisteredClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registered-clients")
@RequiredArgsConstructor
public class RegisteredClientController {

    private final RegisteredClientService registeredClientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredClientResponseDto createRegisteredClient(@Valid @RequestBody RegisteredClientCreateDto dto) {
        RegisteredClientResponseDto createdClient = registeredClientService.createRegisteredClient(dto);
        return createdClient;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRegisteredClient(@PathVariable String id) {
        registeredClientService.deleteRegisteredClient(id);
    }
}
