package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.service.Resource;
import com.academy.springsecurity6full.service.ResourceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST com endpoints protegidos
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SecureController {

    private final ResourceService resourceService;

    // @Secured("ROLE_ADMIN"): Verifica o papel ROLE_ADMIN ANTES da execução.
    // Se a autorização falhar, o método e o serviço não são chamados.
    // Limita-se a verificações estáticas de papéis ou autoridades, sem suporte a SpEL.
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        List<Resource> resources = resourceService.fetchResources(List.of("admin_resource"));
        return ResponseEntity.ok("Admin access: " + resources.get(0).getContent());
    }

    // @PreFilter: Filtra a lista de resourceIds ANTES da execução do método.
    // Mantém apenas IDs onde o usuário autenticado é o dono (baseado em id.split("_")[0]).
    // O serviço recebe a lista filtrada, evitando processamento de recursos não autorizados.
    @PreFilter("filterObject.split('_')[0] == authentication.name")
    @PostMapping("/resources/filter")
    public ResponseEntity<List<Resource>> filterResources(@RequestBody List<String> resourceIds) {
        System.out.println("Filtered resourceIds: " + resourceIds); // Log para depuração
        List<Resource> resources = resourceService.fetchResources(resourceIds);
        return ResponseEntity.ok(resources);
    }

    // @PreFilter com SpEL personalizado: Filtra a lista de recursos ANTES da execução.
    // Usa canAccessResource para verificar cada recurso, mantendo apenas os autorizados.
    // filterTarget especifica o parâmetro a ser filtrado (resources).
    @PreFilter(value = "@resourceService.canAccessResource(authentication, filterObject)", filterTarget = "resources")
    @PostMapping("/resources/custom-filter")
    public ResponseEntity<List<Resource>> customFilterResources(@RequestBody List<Resource> resources) {
        System.out.println("Filtered resources: " + resources); // Log para depuração
        return ResponseEntity.ok(resources);
    }

    // @PreAuthorize: Verifica ANTES da execução se o usuário é o dono do recurso.
    // Usa isResourceOwner com o parâmetro resourceId.
    // O serviço só é chamado se a autorização for aprovada.
    @PreAuthorize("@resourceService.isResourceOwner(authentication, #resourceId)")
    @GetMapping("/resource/pre/{resourceId}")
    public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
        List<Resource> resources = resourceService.fetchResources(List.of(resourceId));
        return ResponseEntity.ok("Access granted: " + resources.get(0).getContent());
    }

    // @PreAuthorize: Verifica ANTES da execução se o usuário tem a autoridade READ_RESOURCE.
    // Usa hasAuthority para restringir o acesso com base em permissões granulares.
    // O serviço só é chamado se a autorização for aprovada.
    @PreAuthorize("hasAuthority('READ_RESOURCE')")
    @GetMapping("/resource/check/{resourceId}")
    public ResponseEntity<String> checkResource(@PathVariable String resourceId) {
        List<Resource> resources = resourceService.fetchResources(List.of(resourceId));
        return ResponseEntity.ok("Read access: " + resources.get(0).getContent());
    }

    // @PostFilter: Filtra a lista de recursos retornada APÓS a execução do método.
    // Usa canAccessResource para manter apenas recursos pertencentes ao usuário autenticado.
    // O serviço é chamado com todos os IDs, mas o resultado final é filtrado.
    @PostFilter("filterObject.owner == authentication.name")
    @GetMapping("/resources/post-filter")
    public List<Resource> getPostFilteredResources() {
        List<String> resourceIds = List.of("john_resource1", "other_resource2", "john_resource3");
        System.out.println("Fetching resources for post-filter: " + resourceIds); // Log para depuração
        return resourceService.fetchResources(resourceIds);
    }
}

