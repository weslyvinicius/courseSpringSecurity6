package com.academy.springsecurity6full.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByName(String name);
}
