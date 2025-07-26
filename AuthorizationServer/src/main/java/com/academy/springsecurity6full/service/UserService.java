package com.academy.springsecurity6full.service;

import com.academy.springsecurity6full.controller.dto.UserCreateDto;
import com.academy.springsecurity6full.controller.dto.UserResponseDto;
import com.academy.springsecurity6full.repository.entity.UserEntity;
import com.academy.springsecurity6full.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        // Verificar se o usuário já existe
        if (userRepository.existsByName(userCreateDto.getName())) {
            throw new RuntimeException("Usuário já existe com o nome: " + userCreateDto.getName());
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(userCreateDto.getName());
        userEntity.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));

        // Criar authorities baseado no DTO
        if (userCreateDto.getAuthorities() != null && !userCreateDto.getAuthorities().isEmpty()) {
            userEntity.createAuthorities(userCreateDto.getAuthorities().toArray(new com.academy.springsecurity6full.repository.entity.AuthorityEnum[0]));
        }

        UserEntity savedUser = userRepository.save(userEntity);

        return convertToResponseDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDto convertToResponseDto(UserEntity userEntity) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(userEntity.getId());
        dto.setName(userEntity.getName());
        dto.setAuthorities(userEntity.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());
        return dto;
    }
}
