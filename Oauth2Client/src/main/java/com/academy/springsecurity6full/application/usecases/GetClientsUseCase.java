package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.ClientDTO;
import com.academy.springsecurity6full.domain.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetClientsUseCase {

    private final ClientService clientService;

    public List<ClientDTO> execute(String grantType) {
        return clientService.getClientsByGrantType(grantType)
                .stream()
                .map(ClientDTO::fromDomain)
                .toList();
    }
}
