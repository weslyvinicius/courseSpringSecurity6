# Spring Security 6 com Spring Boot 3 - Segurança Baseada em Métodos com Persistência

Este projeto demonstra a implementação avançada do Spring Security 6 com Spring Boot 3, utilizando segurança baseada em métodos através da anotação `@PreAuthorize` para controle de acesso granular em APIs REST, com autenticação baseada em banco de dados.

## Estrutura do Projeto

O projeto ilustra o uso de segurança a nível de método em uma aplicação Spring Boot com persistência:

- `SecurityConfig.java`: Configuração central de segurança com habilitação de segurança baseada em método
- `EmployeesController.java`: API REST para funcionários com controle de acesso por método
- `AdminController.java`: API REST para administradores com segurança a nível de classe e método
- `UserEntity.java`: Entidade JPA que implementa UserDetails para autenticação
- `UserAuthorities.java`: Entidade JPA que implementa GrantedAuthority para autorização
- `UserDetailsServiceImpl.java`: Implementação do UserDetailsService para carregar usuários do banco de dados

## Principais Conceitos Demonstrados

### 1. Habilitação da Segurança Baseada em Métodos

No arquivo `SecurityConfig.java`, a configuração habilita a segurança baseada em métodos:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // ...
}
```

A anotação `@EnableMethodSecurity` permite o uso de `@PreAuthorize` e outras anotações de segurança em métodos.

### 2. Configuração de Segurança

A configuração de segurança permite acesso ao logout e console H2, com autenticação necessária para outros recursos:

```java
@Bean
public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(config ->
            config.requestMatchers("/logout").permitAll()
                  .requestMatchers("/h2-console/**").permitAll()
                  .requestMatchers("/h2-console").permitAll()
                  .anyRequest().authenticated());

    // usar autenticação HTTP básica
    http.httpBasic();

    // desabilitar CSRF
    http.csrf().disable();

    http.cors().disable();

    // Habilitar formulário de login
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
    // @PreAuthorize("hasAuthority('DELETE_AUTHORITY')") --> VOCÊ PODE USAR AUTHORITIES
    public String deleteEmployee(@PathVariable String employeeId){
        return "Delete Employee";
    }
}
```

Cada método tem sua própria regra de autorização:
- `getAllOfEmployees()`: Acessível para todos (`permitAll`)
- `getEmployee()`: Requer role EMPLOYEE
- `saveEmployee()` e `updateEmployee()`: Requer role MANAGER
- `deleteEmployee()`: Requer role ADMIN (ou pode ser configurado para usar permissões específicas)

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

### 5. Modelo de Persistência para Autenticação e Autorização

O projeto utiliza entidades JPA para representar usuários e suas autoridades:

#### UserEntity.java
```java
@Entity
@Table(name = "tb_user")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;

    private String name;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "tb_users_authority",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Collection<UserAuthorities> authorities;
    
    // Implementação dos métodos do UserDetails
}
```

#### UserAuthorities.java
```java
@Entity
@Table(name = "tb_authority")
public class UserAuthorities implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private Long id;

    @Column(name = "name")
    @Enumerated(EnumType.STRING)
    private AuthorityEnum authority;

    @Override
    public String getAuthority() {
        return this.authority.name();
    }
}
```

### 6. Serviço de Detalhes do Usuário

A implementação personalizada do `UserDetailsService` carrega usuários do banco de dados:

```java
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username:" + username));
    }
}
```

### 7. Codificador de Senha

O projeto está configurado com uma implementação simples de codificador de senha para desenvolvimento:

```java
@Bean
PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance();
}

// Alternativa recomendada para produção:
// @Bean
// PasswordEncoder passwordEncoder(){
//     return new BCryptPasswordEncoder();
// }
```

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação (certifique-se de que o banco de dados está configurado corretamente)
2. Teste os endpoints com diferentes usuários:

   **API de Funcionários (/api/employees):**
   - GET `/api/employees`: Acessível para todos (público)
   - GET `/api/employees/{id}`: Requer ROLE_EMPLOYEE
   - POST `/api/employees`: Requer ROLE_MANAGER
   - PUT `/api/employees`: Requer ROLE_MANAGER
   - DELETE `/api/employees/{id}`: Requer ROLE_ADMIN (ou autoridade específica DELETE_AUTHORITY)

   **API de Administradores (/api/admin):**
   - GET `/api/admin`: Acessível para todos (público, sobrescreve configuração da classe)
   - Todos os outros endpoints `/api/admin/**`: Requer ROLE_ADMIN

3. Para autenticar, use:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas

## Configuração do Banco de Dados

O projeto utiliza JPA para persistência. As roles devem ser salvas no banco de dados no formato "ROLE_" (ex: "ROLE_ADMIN").

## Vantagens da Segurança Baseada em Métodos

1. **Maior granularidade**: Permite definir regras específicas para cada operação
2. **Código mais limpo**: Separa a lógica de segurança da configuração central
3. **Maior flexibilidade**: Facilita a combinação de diferentes regras de acesso
4. **Melhor legibilidade**: As regras ficam próximas aos métodos que protegem
5. **Suporte a expressões SpEL**: Permite condições complexas de autorização

## Pontos Importantes

- A anotação `@PreAuthorize` suporta expressões SpEL (Spring Expression Language) para regras complexas de autorização
- Anotações a nível de método sobrescrevem anotações a nível de classe
- As roles devem ser salvas no banco de dados no formato "ROLE_" (ex: ROLE_ADMIN)
- Em ambiente de produção, considere:
  - Habilitar CSRF
  - Utilizar BCryptPasswordEncoder em vez de NoOpPasswordEncoder
  - Utilizar HTTPS para proteger as comunicações

## Recursos Adicionais

- [Documentação do Spring Security sobre Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [Expressões SpEL para controle de acesso](https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html)
- [Spring Security 6 - Guia de Autorização](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
