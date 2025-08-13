package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.AuthorizeRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OAuth2StateStore {
    private final ConcurrentHashMap<String, AuthorizeRequestDTO> storage = new ConcurrentHashMap<>();

    public void save(String state, AuthorizeRequestDTO data) {
        storage.put(state, data);
    }

    public AuthorizeRequestDTO get(String state) {
        return storage.remove(state); // Remove após uso
    }
}
