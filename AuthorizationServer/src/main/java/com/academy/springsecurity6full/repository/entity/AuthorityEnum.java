package com.academy.springsecurity6full.repository.entity;

public enum AuthorityEnum {
    // Roles
    ROLE_EMPLOYEE,
    ROLE_MANAGER,
    ROLE_ADMIN,

    // Employee permissions
    READ_EMPLOYEE,
    CREATE_EMPLOYEE,
    UPDATE_EMPLOYEE,
    DELETE_EMPLOYEE,

    // Report permissions
    READ_REPORT,
    CREATE_REPORT,
    UPDATE_REPORT,
    DELETE_REPORT
}
