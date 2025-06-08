package com.academy.springsecurity6full.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// Serviço para lógica personalizada de autorização
@Service
public class SecurityService {

    // Método usado em SpEL para verificar se o usuário autenticado é o dono do recurso.
    // Extrai o nome do usuário do resourceId (ex.: "john_resource" -> "john") e compara com o nome do usuário autenticado.
    public boolean isResourceOwner(Authentication authentication, String resourceId) {
        return authentication.getName().equals(resourceId.split("_")[0]);
    }
}
