package com.academy.springsecurity6full.infrastructure.repository;


import com.academy.springsecurity6full.infrastructure.repository.entity.RegisteredClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRegisteredClientRepository extends JpaRepository<RegisteredClientEntity, String> {

    Optional<RegisteredClientEntity> findByClientId(String clientId);
}
