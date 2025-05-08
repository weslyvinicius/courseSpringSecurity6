# Spring Security 6 com Spring Boot 3 - Controle de Acesso por Roles

Este projeto demonstra a implementação do Spring Security 6 com Spring Boot 3, focando em autenticação em memória e controle de acesso baseado em roles (RBAC - Role-Based Access Control) para uma API REST de funcionários.

## Estrutura do Projeto

O projeto possui uma estrutura que demonstra o controle de acesso com base em diferentes níveis de autorização:

- `SecurityConfig.java`: Configuração de segurança com definição de permissões por roles
- `EmployeesController.java`: API REST para gerenciamento de funcionários com diferentes níveis de acesso
- `application.yml`: Configurações da aplicação

## Principais Conceitos Demonstrados

### 1. Controle de Acesso por Roles

No arquivo `SecurityConfig.java`, a configuração implementa um controle de acesso detalhado baseado em roles:

```java
@Bean
public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(configure ->
        configure
                .requestMatchers(HttpMethod.GET, "/api/employees").hasRole("EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasRole("EMPLOYEE")
                .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/employees").hasRole("MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                // allow do acess to lougout default
                .requestMatchers("/logout").permitAll()
    );

    // use http basic authentication
    http.httpBasic();

    // disable csrf
    http.csrf().disable();

    //Enable form to login
    http.formLogin(Customizer.withDefaults());

    return http.build();
}
```

Nesta configuração:

- **EMPLOYEE**: Pode apenas ler informações de funcionários (GET)
- **MANAGER**: Pode criar (POST) e atualizar (PUT) funcionários, além de ler
- **ADMIN**: Pode excluir (DELETE) funcionários, além de todas as permissões anteriores
- O endpoint `/logout` é público para permitir que qualquer usuário saia do sistema

### 2. API REST com Operações Controladas

O arquivo `EmployeesController.java` implementa os endpoints da API com diferentes operações:

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {

    @GetMapping
    public String getAllOfEmployees(){
        return "Read all employees";
    }

    @GetMapping("/{employeeId}")
    public String getEmployee(@PathVariable String employeeId){
        return "Read Employee";
    }

    @PostMapping
    public String saveEmployee(){
        return "Create Employee";
    }

    @PutMapping
    public String updateEmployee(){
        return "Update Employee";
    }

    @DeleteMapping("/{employeeId}")
    public String deleteEmployee(@PathVariable String employeeId){
        return "Delete Employee";
    }
}
```

Cada endpoint da API está protegido por um nível específico de autorização conforme configurado no `SecurityConfig`.

### 3. Gerenciamento de Usuários em Memória

O sistema utiliza autenticação em memória com três usuários, cada um com diferentes roles:

```java
@Bean
public InMemoryUserDetailsManager userDetailsManager(){
    User.UserBuilder users = User.withDefaultPasswordEncoder();
    UserDetails john =
            users.username("john")
            .password("j123456")
            .roles("EMPLOYEE")
            .build();

    UserDetails mary =
            users.username("mary")
            .password("m123456")
            .roles("MANAGER","EMPLOYEE")
            .build();

    UserDetails susan =
             users.username("susan")
            .password("s123456")
            .roles("MANAGER","EMPLOYEE","ADMIN")
            .build();

    return new InMemoryUserDetailsManager(john, mary, susan);
}
```

- **john**: Tem apenas a role EMPLOYEE
- **mary**: Tem as roles MANAGER e EMPLOYEE
- **susan**: Tem todas as roles: ADMIN, MANAGER e EMPLOYEE

### 4. Hierarquia de Autorização

O projeto implementa uma hierarquia implícita de autorização, onde:

- **EMPLOYEE**: Nível básico (apenas leitura)
- **MANAGER**: Nível intermediário (leitura, criação e atualização)
- **ADMIN**: Nível completo (leitura, criação, atualização e exclusão)

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação
2. Teste os endpoints com diferentes usuários:

   **Usuário john (EMPLOYEE):**
   - Pode acessar:
     - GET `/api/employees`
     - GET `/api/employees/{id}`
   - Não pode acessar:
     - POST `/api/employees`
     - PUT `/api/employees`
     - DELETE `/api/employees/{id}`

   **Usuário mary (MANAGER):**
   - Pode acessar:
     - GET `/api/employees`
     - GET `/api/employees/{id}`
     - POST `/api/employees`
     - PUT `/api/employees`
   - Não pode acessar:
     - DELETE `/api/employees/{id}`

   **Usuário susan (ADMIN):**
   - Pode acessar todos os endpoints

3. Para autenticar, use:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas

## Pontos Importantes

- O método `withDefaultPasswordEncoder()` é deprecated e recomendado apenas para demonstrações
- Em ambiente de produção, considere:
  - Habilitar CSRF
  - Utilizar autenticação com banco de dados
  - Implementar encoder de senha mais robusto (como BCrypt)
  - Utilizar HTTPS para proteger as comunicações

## Recursos Adicionais

- [Documentação do Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Controle de Acesso Baseado em Roles](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Spring Security 6 - Guia de Autorização](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
