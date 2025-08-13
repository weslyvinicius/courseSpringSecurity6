package com.academy.springsecurity6full.domain.client;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    public List<Client> getClientsByGrantType(String grantType) {
        return clientRepository.findByGrantType(grantType);
    }
}
