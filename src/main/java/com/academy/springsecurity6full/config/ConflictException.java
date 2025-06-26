package com.academy.springsecurity6full.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Username already exists")
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
