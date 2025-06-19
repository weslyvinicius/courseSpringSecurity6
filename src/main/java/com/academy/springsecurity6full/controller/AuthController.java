package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.config.JwtUtil;
import com.academy.springsecurity6full.domain.AuthRequest;
import com.academy.springsecurity6full.domain.AuthResponse;
import com.academy.springsecurity6full.domain.RegisterRequest;
import com.academy.springsecurity6full.repository.AuthorityEnum;
import com.academy.springsecurity6full.repository.UserEntity;
import com.academy.springsecurity6full.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication and user registration.
 *
 * - /api/auth/login: Authenticates user and returns JWT.
 * - /api/auth/register: Registers a new user with default ROLE_EMPLOYEE.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        // Generate JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.findByName(registerRequest.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setName(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.createAuthorities(AuthorityEnum.ROLE_EMPLOYEE); // Default role

        // Save user
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
