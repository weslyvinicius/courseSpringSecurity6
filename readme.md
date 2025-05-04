# Spring Security 6 com Spring Boot 3 - Segurança Baseada em Métodos (@PreAuthorize)

Este projeto demonstra a implementação avançada do Spring Security 6 com Spring Boot 3, utilizando segurança baseada em métodos através da anotação `@PreAuthorize` para controle de acesso granular em APIs REST.

## Estrutura do Projeto

O projeto ilustra o uso de segurança a nível de método em uma aplicação Spring Boot:

- `SecurityConfig.java`: Configuração central de segurança com habilitação de segurança baseada em método
- `EmployeesController.java`: API REST para funcionários com controle de acesso por método
- `AdminController.java`: API REST para administradores com segurança a nível de classe e método
- `application.yml`: Configurações da aplicação

## Principais Conceitos Demonstrados

### 1. Habilitação da Segurança Baseada em Métodos

No arquivo `SecurityConfig.java`, a configuração habilita a segurança baseada em métodos:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // ...
}
```

A anotação `@EnableMethodSecurity(prePostEnabled = true)` permite o uso de `@PreAuthorize` e outras anotações de segurança em métodos.

### 2. Configuração de Segurança Simplificada

A configuração de segurança agora é mais simplificada, deixando o controle de acesso para as anotações nos métodos:

```java
@Bean
public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(configure ->
        configure
                // allow do acess to lougout default
                .requestMatchers("/logout").permitAll()
                .anyRequest().authenticated() // all other requests need to be authenticated
    );

    // use http basic authentication
    http.httpBasic();

    // disable csrf
    http.csrf().disable();

    http.cors().disable();

    //Enable form to login
    http.formLogin(Customizer.withDefaults());

    return http.build();
}
```

### 3. Segurança a Nível de Método com @PreAuthorize

O arquivo `EmployeesController.java` demonstra o uso de `@PreAuthorize` a nível de método:

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {

    @GetMapping
    @PreAuthorize("permitAll")
    public String getAllOfEmployees(){
        return "Read all employees";
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getEmployee(@PathVariable String employeeId){
        return "Read Employee";
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String saveEmployee(){
        return "Create Employee";
    }

    @PutMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String updateEmployee(){
        return "Update Employee";
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEmployee(@PathVariable String employeeId){
        return "Delete Employee";
    }
}
```

Cada método tem sua própria regra de autorização:
- `getAllOfEmployees()`: Acessível para todos (`permitAll`)
- `getEmployee()`: Requer role EMPLOYEE
- `saveEmployee()` e `updateEmployee()`: Requer role MANAGER
- `deleteEmployee()`: Requer role ADMIN

### 4. Segurança a Nível de Classe e Método

O arquivo `AdminController.java` demonstra a segurança em múltiplos níveis:

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping
    @PreAuthorize("permitAll")
    public String getAllOfEmployees(){
        return "Read all admin employees";
    }

    @GetMapping("/{employeeId}")
    public String getAdminEmployee(@PathVariable String employeeId){
        return "Read Admin Employee";
    }

    // ...outros métodos...
}
```

Aqui demonstramos:
- Segurança a nível de classe: `@PreAuthorize("hasRole('ADMIN')")` - todos os métodos requerem role ADMIN por padrão
- Sobreposição por método: `@PreAuthorize("permitAll")` - sobrescreve a configuração da classe e permite acesso público

### 5. Gerenciamento de Usuários em Memória

O sistema continua utilizando autenticação em memória com três usuários, cada um com diferentes roles:

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

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação
2. Teste os endpoints com diferentes usuários:

   **API de Funcionários (/api/employees):**
   - GET `/api/employees`: Acessível para todos (público)
   - GET `/api/employees/{id}`: Requer ROLE_EMPLOYEE (john, mary, susan)
   - POST `/api/employees`: Requer ROLE_MANAGER (mary, susan)
   - PUT `/api/employees`: Requer ROLE_MANAGER (mary, susan)
   - DELETE `/api/employees/{id}`: Requer ROLE_ADMIN (susan)

   **API de Administradores (/api/admin):**
   - GET `/api/admin`: Acessível para todos (público, sobrescreve configuração da classe)
   - Todos os outros endpoints `/api/admin/**`: Requer ROLE_ADMIN (susan)

3. Para autenticar, use:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas

## Vantagens da Segurança Baseada em Métodos

1. **Maior granularidade**: Permite definir regras específicas para cada operação
2. **Código mais limpo**: Separa a lógica de segurança da configuração central
3. **Maior flexibilidade**: Facilita a combinação de diferentes regras de acesso
4. **Melhor legibilidade**: As regras ficam próximas aos métodos que protegem
5. **Suporte a expressões SpEL**: Permite condições complexas de autorização

## Pontos Importantes

- A anotação `@PreAuthorize` suporta expressões SpEL (Spring Expression Language) para regras complexas de autorização
- Anotações a nível de método sobrescrevem anotações a nível de classe
- Em ambiente de produção, considere:
  - Habilitar CSRF
  - Utilizar autenticação com banco de dados
  - Implementar encoder de senha mais robusto (como BCrypt)
  - Utilizar HTTPS para proteger as comunicações

## Recursos Adicionais

- [Documentação do Spring Security sobre Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Expressões SpEL para controle de acesso](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)
- [Spring Security 6 - Guia de Autorização](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
