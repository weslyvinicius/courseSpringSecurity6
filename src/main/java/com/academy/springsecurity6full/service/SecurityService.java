package com.academy.springsecurity6full.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// Serviço para lógica personalizada de autorização
@Service
public class SecurityService {

    // Verifica se o usuário autenticado é o dono do recurso.
    // Compara o nome do usuário autenticado com a parte inicial do resourceId (ex.: "john_resource" -> "john").
    public boolean isResourceOwner(Authentication authentication, String resourceId) {
        return authentication.getName().equals(resourceId.split("_")[0]);
    }
}
