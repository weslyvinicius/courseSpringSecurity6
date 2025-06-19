package com.academy.springsecurity6full.domain;

public record RegisterRequest(
        String username,
        String password
) {
}
