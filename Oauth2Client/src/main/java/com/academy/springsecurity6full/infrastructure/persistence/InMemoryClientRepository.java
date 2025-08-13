package com.academy.springsecurity6full.infrastructure.persistence;

import com.academy.springsecurity6full.domain.client.Client;
import com.academy.springsecurity6full.domain.client.ClientRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryClientRepository implements ClientRepository {

    private final List<Client> clients;

    public InMemoryClientRepository() {
        // Exemplos de clientes para teste
        this.clients = List.of(
                new Client(
                        "my-client",
                        "secret",
                        "http://localhost:8080/oauth2/authorize",
                        "http://localhost:8080/oauth2/token",
                        "http://localhost:8081/oauth2/callback",
                        List.of("openid", "profile"),
                        "code",
                        List.of("authorization_code", "refresh_token"),
                        "Google OAuth2"
                )
        );

    }

    @Override
    public List<Client> findAll() {
        return clients;
    }

    @Override
    public Optional<Client> findByClientId(String clientId) {
        return clients.stream()
                .filter(client -> client.clientId().equalsIgnoreCase(clientId))
                .findFirst();
    }

    @Override
    public List<Client> findByGrantType(String grantType) {
        return clients.stream()
                .filter(client -> client.grantType().contains(grantType))
                .toList();
    }
}
