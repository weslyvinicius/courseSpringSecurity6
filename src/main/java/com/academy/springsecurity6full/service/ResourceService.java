package com.academy.springsecurity6full.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Serviço para manipulação de recursos e lógica de autorização
@Service
public class ResourceService {
    // Simula a recuperação de uma lista de recursos
    public List<Resource> fetchResources(List<String> resourceIds) {
        System.out.println("Fetching resources for IDs: " + resourceIds);
        return resourceIds.stream()
                .map(id -> new Resource(id.split("_")[0], "Content for " + id))
                .collect(Collectors.toList());
    }

    // Verifica se o usuário é o dono do recurso com base no resourceId (usado em @PreAuthorize)
    public boolean isResourceOwner(Authentication authentication, String resourceId) {
        System.out.println("Checking ownership for user: " + authentication.getName() + ", resourceId: " + resourceId);
        return authentication.getName().equals(resourceId.split("_")[0]);
    }

    // Verifica se o usuário pode acessar um recurso específico (usado em @PreFilter)
    public boolean canAccessResource(Authentication authentication, Resource resource) {
        System.out.println("Checking access for user: " + authentication.getName() + ", resource owner: " + resource.getOwner());
        return authentication.getName().equals(resource.getOwner());
    }
}
