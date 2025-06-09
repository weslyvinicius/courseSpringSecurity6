package com.academy.springsecurity6full.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

// Serviço que será chamado pelo controlador
@Service
public class ResourceService {

    // Simula uma operação custosa que retorna um recurso
    public Resource fetchResource(String id) {
        // Log para demonstrar que o serviço é executado
        System.out.println("Fetching resource for ID: " + id);
        return new Resource(id.split("_")[0], "Resource content for " + id);
    }

    // Verifica se o usuário pode acessar o recurso (usado em @PostAuthorize)
    public boolean canAccessResource(Authentication authentication, Resource resource) {
        System.out.println("Checking access for user: " + authentication.getName());
        return authentication.getName().equals(resource.getOwner());
    }

    // Verifica se o usuário é o dono do recurso com base no ID (usado em @PreAuthorize)
    public boolean isResourceOwner(Authentication authentication, String resourceId) {
        System.out.println("Checking ownership for user: " + authentication.getName() + ", resourceId: " + resourceId);
        return authentication.getName().equals(resourceId.split("_")[0]);
    }
}
