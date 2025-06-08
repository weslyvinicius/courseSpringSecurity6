# Spring Security 6 - @PreAuthorize vs @Secured

Este projeto demonstra as diferenças entre as anotações `@PreAuthorize` e `@Secured` do Spring Security 6 com Spring Boot 3, comparando suas funcionalidades e casos de uso.

## Estrutura do Projeto

O projeto é composto por três classes principais:

- **`SecurityConfig.java`**: Configuração de segurança com usuários em memória
- **`SecureController.java`**: Controller REST demonstrando @Secured e @PreAuthorize
- **`SecurityService.java`**: Serviço para lógica de autorização personalizada

## Classes Explicadas

### 1. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
    // Configurações...
}
```

**Configurações importantes:**

- `securedEnabled = true`: **Habilita** a anotação `@Secured`
- `prePostEnabled = true`: **Habilita** as anotações `@PreAuthorize` e `@PostAuthorize`
- Ambas as configurações são necessárias para usar as respectivas anotações

**Usuários configurados:**

| Usuário | Senha | Roles | Authorities |
|---------|-------|-------|-------------|
| admin | password | ROLE_ADMIN | READ_RESOURCE, WRITE_RESOURCE |
| manager | password | ROLE_MANAGER | - |
| john | password | ROLE_USER | READ_RESOURCE, WRITE_RESOURCE |

### 2. SecureController.java

Este controller demonstra as diferenças práticas entre `@Secured` e `@PreAuthorize`:

## Comparação: @Secured vs @PreAuthorize

### Características Gerais

| Aspecto | @Secured | @PreAuthorize |
|---------|----------|---------------|
| **Flexibilidade** | Limitada | Alta |
| **SpEL Support** | ❌ Não | ✅ Sim |
| **Lógica Complexa** | ❌ Não | ✅ Sim |
| **Configuração** | `securedEnabled = true` | `prePostEnabled = true` |
| **Performance** | Mais rápida | Ligeiramente mais lenta |
| **Simplicidade** | Mais simples | Mais poderosa |

### Exemplos Práticos

#### 1. Controle por Role Única

**@Secured:**
```java
@Secured("ROLE_ADMIN")
@GetMapping("/admin")
public ResponseEntity<String> adminOnly() {
    return ResponseEntity.ok("Admin access granted");
}
```

**@PreAuthorize (equivalente):**
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin")
public ResponseEntity<String> adminOnly() {
    return ResponseEntity.ok("Admin access granted");
}
```

**Diferenças:**
- `@Secured` usa o nome exato da role: `"ROLE_ADMIN"`
- `@PreAuthorize` usa função: `hasRole('ADMIN')` (adiciona `ROLE_` automaticamente)

#### 2. Controle por Múltiplas Roles

**@Secured:**
```java
@Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
@GetMapping("/manager-or-admin")
public ResponseEntity<String> managerOrAdmin() {
    return ResponseEntity.ok("Manager or Admin access granted");
}
```

**@PreAuthorize (equivalente):**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@GetMapping("/manager-or-admin")
public ResponseEntity<String> managerOrAdmin() {
    return ResponseEntity.ok("Manager or Admin access granted");
}
```

**Diferenças:**
- `@Secured` usa array de strings: `{"ROLE_ADMIN", "ROLE_MANAGER"}`
- `@PreAuthorize` usa função: `hasAnyRole('ADMIN', 'MANAGER')`

#### 3. Controle por Authority Única

**@Secured:**
```java
@Secured("READ_RESOURCE")
@GetMapping("/read")
public ResponseEntity<String> readResource() {
    return ResponseEntity.ok("Read access granted");
}
```

**@PreAuthorize (equivalente):**
```java
@PreAuthorize("hasAuthority('READ_RESOURCE')")
@GetMapping("/read")
public ResponseEntity<String> readResource() {
    return ResponseEntity.ok("Read access granted");
}
```

#### 4. Controle por Múltiplas Authorities

**@Secured:**
```java
@Secured({"READ_RESOURCE", "WRITE_RESOURCE"})
@GetMapping("/read-or-write")
public ResponseEntity<String> readOrWriteResource() {
    return ResponseEntity.ok("Read or Write access granted");
}
```

**@PreAuthorize (equivalente):**
```java
@PreAuthorize("hasAnyAuthority('READ_RESOURCE', 'WRITE_RESOURCE')")
@GetMapping("/read-or-write")
public ResponseEntity<String> readOrWriteResource() {
    return ResponseEntity.ok("Read or Write access granted");
}
```

#### 5. Lógica Personalizada - Só @PreAuthorize

```java
@PreAuthorize("@securityService.isResourceOwner(authentication, #resourceId)")
@GetMapping("/resource/{resourceId}")
public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
    return ResponseEntity.ok("Custom resource access for " + resourceId);
}
```

**⚠️ IMPOSSÍVEL com @Secured!**

`@Secured` não suporta:
- Expressões SpEL
- Chamadas para métodos de services
- Lógica baseada em parâmetros
- Verificações dinâmicas

## Limitações de Cada Anotação

### @Secured - Limitações

❌ **Não suporta:**
- Expressões SpEL
- Lógica AND (`hasRole('ADMIN') AND hasAuthority('WRITE')`)
- Verificações baseadas em parâmetros
- Chamadas para métodos personalizados
- Condições dinâmicas

✅ **Suporta apenas:**
- Lista simples de roles/authorities
- Verificação OR implícita (qualquer uma da lista)

### @PreAuthorize - Limitações

❌ **Desvantagens:**
- Ligeiramente mais lenta (devido ao parsing SpEL)
- Mais complexa para casos simples
- Curva de aprendizado maior

✅ **Vantagens:**
- Suporte completo a SpEL
- Lógica complexa (AND, OR, NOT)
- Verificações baseadas em parâmetros
- Integração com services Spring

## Casos de Uso Recomendados

### Use @Secured quando:

1. **Verificações simples** de role/authority
2. **Performance crítica** (diferença mínima, mas existe)
3. **Equipe menos experiente** com Spring Security
4. **Casos estáticos** onde as regras não mudam

**Exemplo típico:**
```java
@Secured("ROLE_ADMIN")  // Simples e direto
public void adminOnlyMethod() { }
```

### Use @PreAuthorize quando:

1. **Lógica complexa** de autorização
2. **Verificações baseadas em parâmetros**
3. **Combinação de múltiplas condições**
4. **Integração com services personalizados**
5. **Verificações dinâmicas**

**Exemplos típicos:**
```java
// Lógica complexa
@PreAuthorize("hasRole('ADMIN') and hasAuthority('WRITE_RESOURCE')")

// Verificação de parâmetro
@PreAuthorize("#userId == authentication.principal.id")

// Service personalizado
@PreAuthorize("@securityService.canAccess(authentication, #resourceId)")
```

## Expressões SpEL Úteis (Apenas @PreAuthorize)

| Expressão | Descrição |
|-----------|-----------|
| `hasRole('ADMIN')` | Verifica role específica |
| `hasAnyRole('ADMIN', 'MANAGER')` | Qualquer uma das roles |
| `hasAuthority('READ')` | Verifica authority específica |
| `hasAnyAuthority('READ', 'WRITE')` | Qualquer uma das authorities |
| `#param == authentication.name` | Compara parâmetro com usuário |
| `@service.method(args)` | Chama método de bean Spring |
| `hasRole('ADMIN') and hasAuthority('WRITE')` | Lógica AND |
| `hasRole('ADMIN') or hasRole('MANAGER')` | Lógica OR |
| `!hasRole('GUEST')` | Lógica NOT |

## Como Testar

### Comandos curl para teste:

```bash
# Endpoints protegidos por @Secured

# Admin access - Só admin
curl -u admin:password http://localhost:8080/api/admin
curl -u manager:password http://localhost:8080/api/admin  # ← 403 Forbidden

# Manager or Admin - admin e manager
curl -u admin:password http://localhost:8080/api/manager-or-admin
curl -u manager:password http://localhost:8080/api/manager-or-admin
curl -u john:password http://localhost:8080/api/manager-or-admin  # ← 403 Forbidden

# Read access - admin e john (têm READ_RESOURCE)
curl -u admin:password http://localhost:8080/api/read
curl -u john:password http://localhost:8080/api/read
curl -u manager:password http://localhost:8080/api/read  # ← 403 Forbidden

# Read or Write - admin e john
curl -u admin:password http://localhost:8080/api/read-or-write
curl -u john:password http://localhost:8080/api/read-or-write

# Endpoint protegido por @PreAuthorize (lógica personalizada)
curl -u john:password http://localhost:8080/api/resource/john_document
curl -u john:password http://localhost:8080/api/resource/admin_document  # ← 403 Forbidden
```

### Matriz de Acesso

| Endpoint | admin | manager | john |
|----------|-------|---------|------|
| `/api/admin` | ✅ 200 | ❌ 403 | ❌ 403 |
| `/api/manager-or-admin` | ✅ 200 | ✅ 200 | ❌ 403 |
| `/api/read` | ✅ 200 | ❌ 403 | ✅ 200 |
| `/api/read-or-write` | ✅ 200 | ❌ 403 | ✅ 200 |
| `/api/resource/john_doc` | ❌ 403 | ❌ 403 | ✅ 200 |
| `/api/resource/admin_doc` | ❌ 403 | ❌ 403 | ❌ 403 |

## Migração: @Secured → @PreAuthorize

### Conversões Comuns

```java
// Role única
@Secured("ROLE_ADMIN") 
→ @PreAuthorize("hasRole('ADMIN')")

// Múltiplas roles
@Secured({"ROLE_ADMIN", "ROLE_MANAGER"}) 
→ @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")

// Authority única
@Secured("READ_RESOURCE") 
→ @PreAuthorize("hasAuthority('READ_RESOURCE')")

// Múltiplas authorities
@Secured({"READ_RESOURCE", "WRITE_RESOURCE"}) 
→ @PreAuthorize("hasAnyAuthority('READ_RESOURCE', 'WRITE_RESOURCE')")
```

## Recomendações Gerais

### Para Novos Projetos:
**Use @PreAuthorize** - Mais flexível e poderosa, padrão moderno

### Para Projetos Legados:
- **Mantenha @Secured** se já funciona bem
- **Migre gradualmente** para @PreAuthorize quando precisar de mais funcionalidades

### Para Performance Crítica:
- **@Secured** é ligeiramente mais rápida
- Diferença é mínima na maioria dos casos
- Use profiling antes de otimizar

### Para Equipes:
- **@Secured**: Mais fácil para iniciantes
- **@PreAuthorize**: Mais poderosa para desenvolvedores experientes

## Próximos Passos de Estudo

1. **@PostAuthorize**: Verificação após execução do método
2. **@PreFilter/@PostFilter**: Filtragem de coleções
3. **Method Security com JPA**: Integração com entidades
4. **Custom Security Expressions**: Criar suas próprias funções SpEL
5. **Performance Tuning**: Otimização para aplicações grandes

## Observações de Segurança

⚠️ **Este código é apenas para estudos!**

Para produção:
- Use `BCryptPasswordEncoder` em vez de `{noop}`
- Implemente authorities baseadas em banco de dados
- Configure HTTPS
- Use JWT ou OAuth2 para autenticação stateless
- Implemente auditoria de segurança