# Spring Security 6 com Spring Boot 3 - Controle de Acesso por Roles com Banco de Dados

Este projeto demonstra a implementação do Spring Security 6 com Spring Boot 3, focando em autenticação baseada em banco de dados e controle de acesso baseado em roles (RBAC - Role-Based Access Control) para uma API REST de funcionários.

## Estrutura do Projeto

O projeto possui uma estrutura que demonstra o controle de acesso com base em diferentes níveis de autorização:

- `SecurityConfig.java`: Configuração de segurança com definição de permissões por roles
- `EmployeesController.java`: API REST para gerenciamento de funcionários com diferentes níveis de acesso
- `UserEntity.java`: Entidade que implementa UserDetails para autenticação baseada em banco de dados
- `UserAuthorities.java`: Entidade que implementa GrantedAuthority para as roles
- `UserDetailsServiceImpl.java`: Implementação do UserDetailsService para carregar usuários do banco de dados
- `UserRepository.java`: Repositório JPA para acesso aos dados dos usuários
- `AuthorityEnum.java`: Enum para definição das roles disponíveis

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
    
    http.userDetailsService(userDetailsService);

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

### 3. Autenticação Baseada em Banco de Dados

O sistema utiliza autenticação baseada em banco de dados com entidades JPA:

#### Modelo de Dados

- `UserEntity.java`: Implementa a interface UserDetails para autenticação
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
    
    // Implementação dos métodos de UserDetails
}
```

- `UserAuthorities.java`: Implementa a interface GrantedAuthority para as roles
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

#### Serviço de Autenticação

- `UserDetailsServiceImpl.java`: Implementa a interface UserDetailsService para carregar usuários do banco de dados
```java
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

### 4. Hierarquia de Autorização

O projeto define uma hierarquia de autorização através do enum `AuthorityEnum`:

```java
public enum AuthorityEnum {
    ROLE_EMPLOYEE,
    ROLE_MANAGER,
    ROLE_ADMIN
}
```

Essa hierarquia implementa:
- **EMPLOYEE**: Nível básico (apenas leitura)
- **MANAGER**: Nível intermediário (leitura, criação e atualização)
- **ADMIN**: Nível completo (leitura, criação, atualização e exclusão)

## Como Configurar o Banco de Dados

Para configurar o sistema com banco de dados:

1. Configure seu banco de dados no arquivo `application.yml` ou `application.properties`
2. Crie as tabelas necessárias:
   - `tb_user`: Armazena as informações dos usuários
   - `tb_authority`: Armazena as roles disponíveis
   - `tb_users_authority`: Tabela de relacionamento entre usuários e roles

3. Insira dados iniciais para os usuários:
   - Crie usuários com diferentes combinações de roles:
     - Usuário com role EMPLOYEE
     - Usuário com roles EMPLOYEE e MANAGER
     - Usuário com roles EMPLOYEE, MANAGER e ADMIN

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação
2. Autentique-se usando:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas
3. Teste os endpoints com diferentes usuários conforme suas roles

   **Usuário com ROLE_EMPLOYEE:**
   - Pode acessar:
     - GET `/api/employees`
     - GET `/api/employees/{id}`
   - Não pode acessar:
     - POST `/api/employees`
     - PUT `/api/employees`
     - DELETE `/api/employees/{id}`

   **Usuário com ROLE_MANAGER:**
   - Pode acessar:
     - GET `/api/employees`
     - GET `/api/employees/{id}`
     - POST `/api/employees`
     - PUT `/api/employees`
   - Não pode acessar:
     - DELETE `/api/employees/{id}`

   **Usuário com ROLE_ADMIN:**
   - Pode acessar todos os endpoints

## Configuração de Segurança

O projeto está configurado com:

```java
@Bean
PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance();
}
```

> ⚠️ **Nota:** O `NoOpPasswordEncoder` é usado apenas para demonstração e não é recomendado para ambientes de produção.

## Pontos Importantes

- Em ambiente de produção, considere:
  - Habilitar CSRF
  - Implementar encoder de senha mais robusto (como BCrypt)
  ```java
  @Bean
  PasswordEncoder passwordEncoder(){
      return new BCryptPasswordEncoder();
  }
  ```
  - Utilizar HTTPS para proteger as comunicações
  - Implementar expiração de sessão e rotação de tokens
  - Configurar gerenciamento de sessão apropriado

## Recursos Adicionais

- [Documentação do Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Controle de Acesso Baseado em Roles](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Spring Security 6 - Guia de Autorização](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
- [Autenticação com JPA](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html)
