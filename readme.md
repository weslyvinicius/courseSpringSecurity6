# Spring Security 6 com Spring Boot 3 - Utilizando Roles e Authorities

Este projeto demonstra uma implementação avançada do Spring Security 6 com Spring Boot 3, focando na combinação de roles (papéis) e authorities (permissões) para um controle de acesso centralizado e refinado.

## Estrutura do Projeto

O projeto demonstra um sistema de controle de acesso completo baseado em authorities e roles:

- `SecurityConfig.java`: Configuração centralizada de segurança com definição de usuários, roles, authorities e regras de autorização
- `EmployeesController.java`: API REST para funcionários
- `AdminController.java`: API REST para administradores
- `ReportsController.java`: API REST para relatórios com exemplo de combinação de roles e authorities
- `application.yml`: Configurações da aplicação

## Principais Conceitos Demonstrados

### 1. Diferença entre Roles e Authorities

No Spring Security:

- **Roles**: Representam um papel ou função de um usuário no sistema (EMPLOYEE, MANAGER, ADMIN). São tratadas internamente como authorities com o prefixo "ROLE_".
- **Authorities**: Representam permissões específicas para ações (READ_EMPLOYEE, CREATE_EMPLOYEE, etc.).

### 2. Por que Implementamos Apenas com Authorities

No Spring Security, há um comportamento importante que precisa ser compreendido quando se trabalha com roles e authorities simultaneamente:

- **Conversão automática**: Uma role definida com `.roles("ADMIN")` é convertida para uma authority `ROLE_ADMIN`.
- **Conflito potencial**: Definir `.roles()` e `.authorities()` simultaneamente para o mesmo usuário pode causar comportamentos inesperados.
- **Solução adotada**: Para evitar esses problemas, implementamos tudo como authorities, incluindo as roles (prefixadas com "ROLE_"). Isso torna o código mais previsível e evita conflitos.

```java
// Abordagem INCORRETA (usada anteriormente):
UserDetails john = users
        .username("john")
        .password("j123456")
        .roles("EMPLOYEE")          // Isso cria authority "ROLE_EMPLOYEE"
        .authorities("READ_EMPLOYEE") // Isso adiciona outra authority
        .build();

// Abordagem CORRETA (implementada agora):
UserDetails john = users
        .username("john")
        .password("j123456")
        .authorities("ROLE_EMPLOYEE", "READ_EMPLOYEE")
        .build();
```

### 3. Configuração de Usuários com Authorities

No arquivo `SecurityConfig.java`, cada usuário recebe authorities que representam tanto suas roles quanto suas permissões específicas:

```java
@Bean
public InMemoryUserDetailsManager userDetailsManager() {
    User.UserBuilder users = User.withDefaultPasswordEncoder();
    
    // User com role EMPLOYEE e permissões de leitura
    UserDetails john = users
            .username("john")
            .password("j123456")
            .authorities("ROLE_EMPLOYEE", "READ_EMPLOYEE")
            .build();

    // User com role MANAGER e permissões de leitura, criação e atualização
    UserDetails mary = users
            .username("mary")
            .password("m123456")
            .authorities("ROLE_MANAGER", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT")
            .build();

    // User com role ADMIN e todas as permissões
    UserDetails susan = users
            .username("susan")
            .password("s123456")
            .authorities("ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", 
                         "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT")
            .build();

    return new InMemoryUserDetailsManager(john, mary, susan);
}
```

Cada usuário tem:
- **john**: Role EMPLOYEE + authority READ_EMPLOYEE
- **mary**: Role MANAGER + authorities para leitura, criação e atualização
- **susan**: Role ADMIN + todas as authorities

### 4. Configuração Centralizada de Segurança

O controle de acesso é configurado inteiramente no arquivo `SecurityConfig.java` usando o método `authorizeHttpRequests()`. centralizando a lógica de segurança e facilitando a manutenção. As regras são definidas com base em authorities e, em alguns casos, roles (tratadas como authorities com prefixo "ROLE_").

Exemplo para endpoints de funcionários:

```java
.requestMatchers(HttpMethod.GET, "/api/employees").permitAll()
.requestMatchers(HttpMethod.GET, "/api/employees/**").hasAuthority("READ_EMPLOYEE")
.requestMatchers(HttpMethod.POST, "/api/employees").hasAuthority("CREATE_EMPLOYEE")
.requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAuthority("UPDATE_EMPLOYEE")
.requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAuthority("DELETE_EMPLOYEE")
```

Exemplo para endpoints de administradores:

```java
.requestMatchers(HttpMethod.GET, "/api/admin").permitAll()
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

### 5. Ordem de Precedência das Regras de Autorização

As regras de autorização no Spring Security são avaliadas na **ordem em que são definidas** no método `authorizeHttpRequests()`. A primeira regra que corresponde à requisição determina se o acesso será concedido ou negado. Isso torna a ordem das regras crítica:

- **Regras específicas primeiro**: Regras para endpoints exatos (ex.: `/api/reports/combined-auth`) devem ser colocadas antes de regras para padrões genéricos (ex.: `/api/reports/**`).
- **Impacto de regras genéricas**: Se uma regra genérica for definida antes de uma específica, ela pode interceptar a requisição prematuramente, levando a autorizações ou negações incorretas.
- **Boa prática**: Sempre organize as regras do mais específico para o mais genérico para garantir que a lógica de autorização seja aplicada corretamente.

Exemplo no `SecurityConfig.java`:

```java
// Endpoint específico de Reports (colocado antes das regras genéricas)
.requestMatchers(HttpMethod.GET, "/api/reports/combined-auth").access(...)
// Endpoints de Reports (genéricos)
.requestMatchers(HttpMethod.GET, "/api/reports").hasAuthority("READ_REPORT")
.requestMatchers(HttpMethod.GET, "/api/reports/**").hasAuthority("READ_REPORT")
```

### 6. Combinando Roles e Authorities

O endpoint `/api/reports/combined-auth` demonstra a combinação de roles e authorities com uma lógica personalizada no `SecurityConfig.java`:

```java
.requestMatchers(HttpMethod.GET, "/api/reports/combined-auth").access(
    (authentication, object) -> {
        Optional<Authentication> auth = Optional.ofNullable(authentication.get());
        return auth
            .map(a -> a.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER")) &&
                a.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("READ_REPORT")))
            .map(AuthorizationDecision::new)
            .orElse(new AuthorizationDecision(false));
    }
)
```

Este endpoint exige que o usuário tenha **ambas** as authorities `ROLE_MANAGER` e `READ_REPORT`. Apenas o usuário `mary` atende a esses critérios.

## Estrutura de Controle de Acesso

### Authorities Implementadas

**Funcionários:**
- READ_EMPLOYEE: Leitura de dados de funcionários
- CREATE_EMPLOYEE: Criação de funcionários
- UPDATE_EMPLOYEE: Atualização de funcionários
- DELETE_EMPLOYEE: Exclusão de funcionários

**Relatórios:**
- READ_REPORT: Leitura de relatórios
- CREATE_REPORT: Criação de relatórios
- UPDATE_REPORT: Atualização de relatórios
- DELETE_REPORT: Exclusão de relatórios

**Roles (como authorities):**
- ROLE_EMPLOYEE: Papel de funcionário comum
- ROLE_MANAGER: Papel de gerente
- ROLE_ADMIN: Papel de administrador

### Roles e Suas Authorities

- **ROLE_EMPLOYEE** (john):
  - READ_EMPLOYEE

- **ROLE_MANAGER** (mary):
  - READ_EMPLOYEE
  - CREATE_EMPLOYEE
  - UPDATE_EMPLOYEE
  - READ_REPORT

- **ROLE_ADMIN** (susan):
  - Todas as authorities

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação
2. Teste os endpoints com diferentes usuários:

   **API de Funcionários (/api/employees):**
   
   | Endpoint | Método | Authority Necessária | Usuários com Acesso |
   |----------|--------|---------------------|---------------------|
   | `/api/employees` | GET | nenhuma (permitAll) | Todos |
   | `/api/employees/{id}` | GET | READ_EMPLOYEE | john, mary, susan |
   | `/api/employees` | POST | CREATE_EMPLOYEE | mary, susan |
   | `/api/employees/{id}` | PUT | UPDATE_EMPLOYEE | mary, susan |
   | `/api/employees/{id}` | DELETE | DELETE_EMPLOYEE | susan |

   **API de Relatórios (/api/reports):**
   
   | Endpoint | Método | Authority Necessária | Usuários com Acesso |
   |----------|--------|---------------------|---------------------|
   | `/api/reports` | GET | READ_REPORT | mary, susan |
   | `/api/reports/{id}` | GET | READ_REPORT | mary, susan |
   | `/api/reports` | POST | CREATE_REPORT | susan |
   | `/api/reports/{id}` | PUT | UPDATE_REPORT | susan |
   | `/api/reports/{id}` | DELETE | DELETE_REPORT | susan |
   | `/api/reports/combined-auth` | GET | READ_REPORT + ROLE_MANAGER | mary |

   **API de Administradores (/api/admin):**
   
   | Endpoint | Método | Authority Necessária | Usuários com Acesso |
   |----------|--------|---------------------|---------------------|
   | `/api/admin` | GET | nenhuma (permitAll) | Todos |
   | `/api/admin/**` | Outros | ROLE_ADMIN | susan |

3. Para autenticar, use:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas

## Vantagens desta Abordagem

1. **Centralização**: Todo o controle de acesso é definido no `SecurityConfig.java`, facilitando a manutenção e auditoria.
2. **Controle granular**: Authorities permitem definir permissões específicas para operações individuais.
3. **Consistência interna**: Tratar roles e permissions uniformemente como authorities evita comportamentos inesperados.
4. **Flexibilidade**: A lógica personalizada (como no endpoint `/api/reports/combined-auth`) permite esquemas de autorização complexos.
5. **Expressividade**: As regras de segurança expressam claramente a intenção (ex.: `hasAuthority('DELETE_EMPLOYEE')`).

## Pontos Importantes

- O método `withDefaultPasswordEncoder()` é deprecado e recomendado apenas para demonstrações.
- Em ambiente de produção:
  - Implemente authorities baseadas em banco de dados.
  - Use encoder de senha mais robusto (como BCrypt).
  - Considere implementar mecanismos de cache para authorities.
  - Habilite CSRF e use HTTPS.

## Recursos Adicionais

- [Documentação do Spring Security sobre Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Diferença entre Roles e Authorities no Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#appendix-faq-role-vs-authority)
- [Configuração de HttpSecurity](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)