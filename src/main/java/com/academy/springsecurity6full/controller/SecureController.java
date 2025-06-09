package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.service.Resource;
import com.academy.springsecurity6full.service.ResourceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SecureController {

    private final ResourceService resourceService;

    // @Secured("ROLE_ADMIN"): Verifica o papel ROLE_ADMIN ANTES da execução.
    // Se a autorização falhar, o método do controlador e o serviço não são chamados.
    // Não suporta SpEL, sendo limitado a papéis ou autoridades estáticas.
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        // Serviço não é chamado se a autorização falhar
        Resource resource = resourceService.fetchResource("admin_resource");
        return ResponseEntity.ok("Admin access: " + resource.getContent());
    }

    // @PostAuthorize: Verifica APÓS a execução do método e a chamada ao serviço.
    // Checa se o usuário autenticado é o dono do recurso retornado.
    // O serviço é executado antes da validação, mesmo que a autorização falhe.
    @PostAuthorize("returnObject.owner == authentication.name")
    @GetMapping("/resource/owner/{id}")
    public Resource getResourceByOwner(@PathVariable String id) {
        // Serviço é chamado antes da autorização
        return resourceService.fetchResource(id);
    }

    // @PostAuthorize com SpEL personalizado: Verifica APÓS a execução se o usuário pode acessar o recurso.
    // Chama o método canAccessResource do serviço, que é executado após a chamada ao fetchResource.
    @PostAuthorize("@resourceService.canAccessResource(authentication, returnObject)")
    @GetMapping("/resource/custom/{id}")
    public Resource getCustomResource(@PathVariable String id) {
        // Serviço é chamado antes da autorização
        return resourceService.fetchResource(id);
    }

    // @PreAuthorize: Verifica ANTES da execução se o usuário é o dono do recurso.
    // Usa isResourceOwner, que aceita uma String (resourceId), corrigindo o erro de tipo.
    // O serviço fetchResource só é chamado se a autorização for aprovada.
    @PreAuthorize("@resourceService.isResourceOwner(authentication, #id)")
    @GetMapping("/resource/pre/{id}")
    public ResponseEntity<String> accessCustomResource(@PathVariable String id) {
        // Serviço é chamado apenas se a autorização for aprovada
        Resource resource = resourceService.fetchResource(id);
        return ResponseEntity.ok("Access granted: " + resource.getContent());
    }
}


