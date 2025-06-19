package com.academy.springsecurity6full.domain;


/**
 * DTO for login request.
 */
public record AuthRequest (
         String username,
         String password
)  {
}