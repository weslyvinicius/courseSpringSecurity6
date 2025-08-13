package com.academy.springsecurity6full.domain.client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {

    List<Client> findAll();

    Optional<Client> findByClientId(String clientId);

    List<Client> findByGrantType(String grantType);

}
