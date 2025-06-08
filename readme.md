# Spring Security 6 - Estudo da Anotação @PreAuthorize

Este projeto demonstra o uso da anotação `@PreAuthorize` do Spring Security 6 com Spring Boot 3, explorando diferentes formas de controle de acesso a métodos e endpoints.

## Estrutura do Projeto

O projeto é composto por três classes principais:

- **`SecurityConfig.java`**: Configuração de segurança com usuários em memória
- **`SecureController.java`**: Controller REST demonstrando diferentes usos do `@PreAuthorize`
- **`SecurityService.java`**: Serviço para lógica de autorização personalizada

## Classes Explicadas

### 1. SecurityConfig.java

Esta classe é responsável pela configuração de segurança da aplicação:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← Essencial para @PreAuthorize funcionar
public class SecurityConfig {
    // Configurações...
}
```

**Pontos importantes:**

- `@EnableMethodSecurity`: **Obrigatória** para que as anotações de segurança em métodos (como `@PreAuthorize`) funcionem
- Define a cadeia de filtros de segurança
- Configura autenticação HTTP Basic para facilitar os testes
- Cria usuários em memória com diferentes roles e authorities

**Usuários configurados:**

| Usuário | Senha | Roles | Authorities |
|---------|-------|-------|-------------|
| admin | password | ROLE_ADMIN | READ_RESOURCE, WRITE_RESOURCE |
| manager | password | ROLE_MANAGER | - |
| john | password | ROLE_USER | READ_RESOURCE, WRITE_RESOURCE |

### 2. SecureController.java

Esta é a classe principal do estudo, demonstrando diferentes variações da anotação `@PreAuthorize`:

#### 2.1 Controle por Role Específica

```java
@PreAuthorize("hasRole('ROLE_ADMIN')")
@GetMapping("/admin")
public ResponseEntity<String> adminOnly() {
    return ResponseEntity.ok("Admin access granted");
}
```

- **Função**: Permite acesso apenas para usuários com role `ROLE_ADMIN`
- **Quem pode acessar**: Apenas o usuário `admin`
- **Uso**: Endpoints exclusivos para administradores

#### 2.2 Controle por Múltiplas Roles

```java
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
@GetMapping("/manager-or-admin")
public ResponseEntity<String> managerOrAdmin() {
    return ResponseEntity.ok("Manager or Admin access granted");
}
```

- **Função**: Permite acesso para usuários com qualquer uma das roles especificadas
- **Quem pode acessar**: Usuários `admin` e `manager`
- **Uso**: Endpoints para níveis hierárquicos superiores

#### 2.3 Controle por Authority Específica

```java
@PreAuthorize("hasAuthority('READ_RESOURCE')")
@GetMapping("/read")
public ResponseEntity<String> readResource() {
    return ResponseEntity.ok("Read access granted");
}
```

- **Função**: Permite acesso apenas para usuários com a permissão `READ_RESOURCE`
- **Quem pode acessar**: Usuários `admin` e `john`
- **Uso**: Controle granular de permissões específicas

#### 2.4 Controle por Múltiplas Authorities

```java
@PreAuthorize("hasAnyAuthority('READ_RESOURCE', 'WRITE_RESOURCE')")
@GetMapping("/read-or-write")
public ResponseEntity<String> readOrWriteResource() {
    return ResponseEntity.ok("Read or Write access granted");
}
```

- **Função**: Permite acesso para usuários com qualquer uma das authorities especificadas
- **Quem pode acessar**: Usuários `admin` e `john`
- **Uso**: Endpoints que aceitam diferentes tipos de permissão

#### 2.5 Controle Baseado em Parâmetros (SpEL)

```java
@PreAuthorize("#username == authentication.principal.username")
@GetMapping("/user/{username}")
public ResponseEntity<String> accessUserData(@PathVariable String username) {
    return ResponseEntity.ok("User data for " + username);
}
```

- **Função**: Permite que usuários acessem apenas seus próprios dados
- **Como funciona**: Compara o parâmetro `username` da URL com o nome do usuário autenticado
- **Exemplo**: `/api/user/john` só pode ser acessado pelo usuário `john`
- **Uso**: Proteção de dados pessoais do usuário

#### 2.6 Controle com Lógica Personalizada

```java
@PreAuthorize("@securityService.isResourceOwner(authentication, #resourceId)")
@GetMapping("/resource/{resourceId}")
public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
    return ResponseEntity.ok("Custom resource access for " + resourceId);
}
```

- **Função**: Delega a verificação de autorização para um método personalizado
- **Como funciona**: Chama o método `isResourceOwner` do `SecurityService`
- **Exemplo**: `/api/resource/john_document` só pode ser acessado pelo usuário `john`
- **Uso**: Lógicas complexas de autorização

### 3. SecurityService.java

Serviço que implementa lógica de autorização personalizada:

```java
@Service
public class SecurityService {
    public boolean isResourceOwner(Authentication authentication, String resourceId) {
        return authentication.getName().equals(resourceId.split("_")[0]);
    }
}
```

- **Função**: Verifica se o usuário é "dono" do recurso
- **Lógica**: Extrai o nome do usuário do `resourceId` e compara com o usuário autenticado
- **Exemplo**: Para `resourceId = "john_document"`, só o usuário `john` terá acesso

## Diferenças Importantes

### hasRole() vs hasAuthority()

| Aspecto | hasRole() | hasAuthority() |
|---------|-----------|----------------|
| **Prefixo** | Adiciona automaticamente "ROLE_" | Usa o valor exato |
| **Uso** | Papéis/funções do usuário | Permissões específicas |
| **Exemplo** | `hasRole('ADMIN')` → verifica `ROLE_ADMIN` | `hasAuthority('READ_RESOURCE')` |
| **Flexibilidade** | Menos flexível | Mais granular |

### Expressões SpEL Úteis

| Expressão | Descrição |
|-----------|-----------|
| `hasRole('ADMIN')` | Verifica se possui role ADMIN |
| `hasAnyRole('ADMIN', 'USER')` | Qualquer uma das roles |
| `hasAuthority('READ')` | Verifica authority específica |
| `hasAnyAuthority('READ', 'WRITE')` | Qualquer uma das authorities |
| `#param == authentication.name` | Compara parâmetro com usuário |
| `@service.method(args)` | Chama método de bean Spring |

## Como Testar

### 1. Inicie a aplicação
### 2. Teste os endpoints com diferentes usuários:

**Usando curl com autenticação básica:**

```bash
# Teste como admin (acesso total)
curl -u admin:password http://localhost:8080/api/admin
curl -u admin:password http://localhost:8080/api/manager-or-admin
curl -u admin:password http://localhost:8080/api/read

# Teste como manager (acesso limitado)
curl -u manager:password http://localhost:8080/api/manager-or-admin
curl -u manager:password http://localhost:8080/api/admin  # ← Deve retornar 403

# Teste como john (acesso baseado em authorities)
curl -u john:password http://localhost:8080/api/read
curl -u john:password http://localhost:8080/api/user/john
curl -u john:password http://localhost:8080/api/resource/john_document

# Teste de acesso negado
curl -u john:password http://localhost:8080/api/user/admin  # ← Deve retornar 403
```

### 3. Resultados Esperados

| Endpoint | admin | manager | john |
|----------|-------|---------|------|
| `/api/admin` | ✅ 200 | ❌ 403 | ❌ 403 |
| `/api/manager-or-admin` | ✅ 200 | ✅ 200 | ❌ 403 |
| `/api/read` | ✅ 200 | ❌ 403 | ✅ 200 |
| `/api/user/john` | ❌ 403 | ❌ 403 | ✅ 200 |
| `/api/resource/john_doc` | ❌ 403 | ❌ 403 | ✅ 200 |

## Pontos de Aprendizado

### 1. **@EnableMethodSecurity é obrigatória**
Sem ela, as anotações `@PreAuthorize` são ignoradas.

### 2. **SpEL é poderoso**
Permite lógicas complexas diretamente na anotação.

### 3. **Flexibilidade de Authorities**
Authorities oferecem controle mais granular que roles.

### 4. **Integração com Services**
Pode chamar métodos de beans Spring para lógicas complexas.

### 5. **Segurança por Parâmetros**
Permite proteção baseada nos dados da requisição.

## Próximos Passos de Estudo

1. **@PostAuthorize**: Autorização após execução do método
2. **@Secured**: Alternativa mais simples ao @PreAuthorize
3. **Method Security com banco de dados**: Implementação real
4. **Combinações complexas**: Usar operadores lógicos (AND, OR)
5. **Performance**: Cache de authorities para aplicações grandes

## Observações de Segurança

⚠️ **Este código é apenas para estudos!**

Para produção:
- Use `BCryptPasswordEncoder` em vez de `{noop}`
- Implemente authorities baseadas em banco de dados
- Configure HTTPS
- Habilite proteção CSRF quando necessário
- Use JWT ou OAuth2 para autenticação stateless