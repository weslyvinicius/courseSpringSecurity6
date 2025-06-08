package com.academy.springsecurity6full.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST com endpoints protegidos por diferentes usos de @PreAuthorize
@RestController
@RequestMapping("/api")
public class SecureController {

    // @PreAuthorize com hasRole('ROLE_ADMIN'): Restringe o acesso a usuários com o papel ROLE_ADMIN.
    // O Spring Security verifica se o usuário autenticado possui o papel especificado (ROLE_ADMIN).
    // Se o usuário não tiver esse papel, uma exceção AccessDeniedException será lançada (status 403).
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Admin access granted");
    }

    // @PreAuthorize com hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER'): Permite acesso a usuários que possuem
    // pelo menos um dos papéis listados (ROLE_ADMIN ou ROLE_MANAGER).
    // Útil quando múltiplos papéis podem acessar o mesmo recurso.
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/manager-or-admin")
    public ResponseEntity<String> managerOrAdmin() {
        return ResponseEntity.ok("Manager or Admin access granted");
    }

    // @PreAuthorize com hasAuthority('READ_RESOURCE'): Restringe o acesso a usuários com a permissão específica READ_RESOURCE.
    // Diferente de hasRole, hasAuthority verifica permissões granulares (não necessariamente papéis com prefixo ROLE_).
    @PreAuthorize("hasAuthority('READ_RESOURCE')")
    @GetMapping("/read")
    public ResponseEntity<String> readResource() {
        return ResponseEntity.ok("Read access granted");
    }

    // @PreAuthorize com hasAnyAuthority('READ_RESOURCE', 'WRITE_RESOURCE'): Permite acesso a usuários que possuem
    // pelo menos uma das permissões listadas (READ_RESOURCE ou WRITE_RESOURCE).
    // Útil para cenários onde diferentes permissões concedem acesso ao mesmo recurso.
    @PreAuthorize("hasAnyAuthority('READ_RESOURCE', 'WRITE_RESOURCE')")
    @GetMapping("/read-or-write")
    public ResponseEntity<String> readOrWriteResource() {
        return ResponseEntity.ok("Read or Write access granted");
    }

    // @PreAuthorize com SpEL (Spring Expression Language): Verifica se o parâmetro username é igual ao nome do usuário autenticado.
    // Usa a expressão #username == authentication.principal.username para garantir que o usuário só acesse seus próprios dados.
    // #username refere-se ao parâmetro do método, e authentication.principal.username é o nome do usuário logado.
    @PreAuthorize("#username == authentication.principal.username")
    @GetMapping("/user/{username}")
    public ResponseEntity<String> accessUserData(@PathVariable String username) {
        return ResponseEntity.ok("User data for " + username);
    }

    // @PreAuthorize com SpEL personalizado: Chama o método isResourceOwner do SecurityService, passando o objeto Authentication
    // e o parâmetro resourceId. Permite lógica de autorização complexa, como verificar se o usuário é o dono do recurso.
    // O @ antes de securityService indica que é um bean Spring, e a lógica é delegada ao método isResourceOwner.
    @PreAuthorize("@securityService.isResourceOwner(authentication, #resourceId)")
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
        return ResponseEntity.ok("Custom resource access for " + resourceId);
    }
}


