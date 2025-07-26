package com.academy.springsecurity6full.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private List<String> authorities;
}
