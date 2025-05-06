# Spring Security 6 com Spring Boot 3 - Utilizando Roles e Authorities

Este projeto demonstra uma implementação avançada do Spring Security 6 com Spring Boot 3, focando na combinação de roles (papéis) e authorities (permissões) para um controle de acesso mais refinado.

## Estrutura do Projeto

O projeto demonstra um sistema de controle de acesso completo baseado em authorities e roles:

- `SecurityConfig.java`: Configuração de segurança com definição de usuários, roles e authorities
- `EmployeesController.java`: API REST para funcionários com permissões baseadas em authorities
- `AdminController.java`: API REST para administradores com segurança baseada em roles
- `ReportsController.java`: API REST para relatórios com exemplo de combinação de roles e authorities
- `application.yml`: Configurações da aplicação

## Principais Conceitos Demonstrados

### 1. Diferença entre Roles e Authorities

No Spring Security:

- **Roles**: Representam um papel ou função de um usuário no sistema (EMPLOYEE, MANAGER, ADMIN)
- **Authorities**: Representam permissões específicas para ações (READ_EMPLOYEE, CREATE_EMPLOYEE, etc.)

### 2. Por que Implementamos Apenas com Authorities

No Spring Security, há um comportamento importante que precisa ser compreendido quando se trabalha com roles e authorities simultaneamente:

- **Conversão automática**: Uma role é automaticamente convertida para uma authority com prefixo "ROLE_"
- **Conflito potencial**: Quando definimos simultaneamente `.roles()` e `.authorities()` para o mesmo usuário, o Spring pode ter comportamentos inesperados
- **Solução adotada**: Para evitar esses problemas, implementamos tudo como authorities, incluindo as roles (prefixadas com "ROLE_")

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

Esta abordagem torna o código mais previsível e evita problemas de integração entre roles e authorities.

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

Cada usuário agora tem:
- **john**: Role EMPLOYEE + authority READ_EMPLOYEE
- **mary**: Role MANAGER + authorities para leitura, criação e atualização
- **susan**: Role ADMIN + todas as authorities

### 4. Segurança Baseada em Authorities

No arquivo `EmployeesController.java`, os endpoints são protegidos com base em authorities específicas:

```java
@GetMapping("/{employeeId}")
@PreAuthorize("hasAuthority('READ_EMPLOYEE')")
public String getEmployee(@PathVariable String employeeId) {
    return "Read Employee: " + employeeId;
}

@PostMapping
@PreAuthorize("hasAuthority('CREATE_EMPLOYEE')")
public String saveEmployee() {
    return "Create Employee";
}

@PutMapping("/{employeeId}")
@PreAuthorize("hasAuthority('UPDATE_EMPLOYEE')")
public String updateEmployee(@PathVariable String employeeId) {
    return "Update Employee: " + employeeId;
}

@DeleteMapping("/{employeeId}")
@PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
public String deleteEmployee(@PathVariable String employeeId) {
    return "Delete Employee: " + employeeId;
}
```

### 5. Segurança Baseada em Roles

O arquivo `AdminController.java` continua usando controle de acesso baseado em roles, mas agora usando hasAuthority com o prefixo ROLE_:

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // ou pode usar hasRole('ADMIN')
public class AdminController {
    // Métodos protegidos pela role ADMIN
}
```

### 6. Combinando Roles e Authorities

No arquivo `ReportsController.java`, demonstramos como combinar roles e authorities:

```java
@GetMapping("/combined-auth")
@PreAuthorize("hasAuthority('READ_REPORT') and hasAuthority('ROLE_MANAGER')")  // ou hasRole('MANAGER')
public String getReportWithCombinedAuth() {
    return "This endpoint requires both READ_REPORT authority and MANAGER role";
}
```

Este endpoint requer tanto a authority específica quanto a role apropriada.

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

1. **Controle granular**: Authorities permitem definir permissões específicas para operações individuais
2. **Consistência interna**: Tratar roles e permissions uniformemente como authorities evita comportamentos inesperados
3. **Flexibilidade**: Combinação de roles e authorities permite esquemas de autorização complexos
4. **Expressividade**: As regras de segurança expressam claramente a intenção (ex: "hasAuthority('DELETE_EMPLOYEE')")
5. **Compatibilidade**: Tanto `hasRole('ADMIN')` quanto `hasAuthority('ROLE_ADMIN')` funcionam com esta implementação

## Pontos Importantes

- O método `withDefaultPasswordEncoder()` é deprecado e recomendado apenas para demonstrações
- Em ambiente de produção:
  - Implemente authorities baseadas em banco de dados
  - Use encoder de senha mais robusto (como BCrypt)
  - Considere implementar mecanismos de cache para authorities
  - Habilite CSRF e use HTTPS

## Recursos Adicionais

- [Documentação do Spring Security sobre Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Diferença entre Roles e Authorities no Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#appendix-faq-role-vs-authority)
- [Expressões SpEL para controle de acesso](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)