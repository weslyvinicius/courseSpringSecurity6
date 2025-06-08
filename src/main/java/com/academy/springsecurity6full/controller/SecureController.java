package com.academy.springsecurity6full.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST com endpoints protegidos por @Secured e uma instância de @PreAuthorize
@RestController
@RequestMapping("/api")
public class SecureController {

    // @Secured("ROLE_ADMIN"): Restringe o acesso a usuários com o papel ROLE_ADMIN.
    // A anotação @Secured verifica papéis ou autoridades específicas, mas não suporta expressões SpEL.
    // Se o usuário não tiver ROLE_ADMIN, o Spring Security lança AccessDeniedException (status 403).
    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Admin access granted");
    }

    // @Secured({"ROLE_ADMIN", "ROLE_MANAGER"}): Permite acesso a usuários com ROLE_ADMIN ou ROLE_MANAGER.
    // Similar a hasAnyRole em @PreAuthorize, mas limitado a uma lista estática de papéis.
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/manager-or-admin")
    public ResponseEntity<String> managerOrAdmin() {
        return ResponseEntity.ok("Manager or Admin access granted");
    }

    // @Secured("READ_RESOURCE"): Restringe o acesso a usuários com a autoridade READ_RESOURCE.
    // @Secured pode verificar autoridades granulares, mas não permite lógica dinâmica.
    @Secured("READ_RESOURCE")
    @GetMapping("/read")
    public ResponseEntity<String> readResource() {
        return ResponseEntity.ok("Read access granted");
    }

    // @Secured({"READ_RESOURCE", "WRITE_RESOURCE"}): Permite acesso a usuários com READ_RESOURCE ou WRITE_RESOURCE.
    // Similar a hasAnyAuthority em @PreAuthorize, mas sem suporte a SpEL.
    @Secured({"READ_RESOURCE", "WRITE_RESOURCE"})
    @GetMapping("/read-or-write")
    public ResponseEntity<String> readOrWriteResource() {
        return ResponseEntity.ok("Read or Write access granted");
    }

    // @PreAuthorize com SpEL personalizado: Usa Spring Expression Language para chamar o método isResourceOwner.
    // Diferente de @Secured, @PreAuthorize suporta lógica dinâmica e personalizada via SpEL.
    // Aqui, verifica se o usuário autenticado é o dono do recurso com base no resourceId.
    @PreAuthorize("@securityService.isResourceOwner(authentication, #resourceId)")
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
        return ResponseEntity.ok("Custom resource access for " + resourceId);
    }
}


