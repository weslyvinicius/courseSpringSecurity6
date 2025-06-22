package com.academy.springsecurity6full.dto;


/**
 * DTO for login request.
 */
public record AuthRequest (
         String username,
         String password
)  {
}