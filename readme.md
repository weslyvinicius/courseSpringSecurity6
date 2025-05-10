# Spring Security 6 com Spring Boot 3 - Utilizando Roles e Authorities com Persistência

Este projeto demonstra uma implementação avançada do Spring Security 6 com Spring Boot 3, focando na combinação de roles (papéis) e authorities (permissões) para um controle de acesso centralizado e refinado, utilizando persistência em banco de dados.

## Estrutura do Projeto

O projeto implementa um sistema de controle de acesso completo baseado em authorities e roles com persistência:

- `SecurityConfig.java`: Configuração centralizada de segurança com regras de autorização
- `UserEntity.java`: Entidade JPA que implementa UserDetails para autenticação
- `UserAuthorities.java`: Entidade JPA que implementa GrantedAuthority para autorização
- `AuthorityEnum.java`: Enumeração com todas as authorities disponíveis
- `UserRepository.java`: Interface para persistência de usuários
- `UserDetailsServiceImpl.java`: Serviço para carregamento de usuários do banco de dados
- `EmployeesController.java`: API REST para funcionários
- `AdminController.java`: API REST para administradores
- `ReportsController.java`: API REST para relatórios com exemplo de combinação de roles e authorities

## Principais Conceitos Demonstrados

### 1. Diferença entre Roles e Authorities

No Spring Security:

- **Roles**: Representam um papel ou função de um usuário no sistema (EMPLOYEE, MANAGER, ADMIN). São tratadas internamente como authorities com o prefixo "ROLE_".
- **Authorities**: Representam permissões específicas para ações (READ_EMPLOYEE, CREATE_EMPLOYEE, etc.).

### 2. Implementação com Persistência de Usuários e Authorities

A nova implementação utiliza entidades JPA para persistir usuários e suas authorities:

- `UserEntity`: Implementa a interface `UserDetails` e mantém a relação com suas authorities
- `UserAuthorities`: Implementa a interface `GrantedAuthority` e armazena as permissões
- `AuthorityEnum`: Define todas as authorities possíveis, incluindo roles (com prefixo "ROLE_")

```java
// Entidade de usuário que implementa UserDetails
@Entity
@Table(name = "tb_user")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String password;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "tb_users_authority",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Collection<UserAuthorities> authorities = new ArrayList<>();
    
    // Método auxiliar para criar authorities
    public void createAuthorities(AuthorityEnum... authorities) {
        for (AuthorityEnum authority : authorities) {
            UserAuthorities userAuthorities = new UserAuthorities();
            userAuthorities.setAuthority(authority);
            this.authorities.add(userAuthorities);
        }
    }
    
    // Implementação dos métodos de UserDetails
    // ...
}
```

### 3. Carregamento de Usuários do Banco de Dados

O serviço `UserDetailsServiceImpl` busca usuários no banco de dados através do `UserRepository`:

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

### 4. Configuração Centralizada de Segurança

O controle de acesso continua sendo configurado no arquivo `SecurityConfig.java` usando o método `authorizeHttpRequests()`, mas agora integrado com o serviço de usuários baseado em banco de dados:

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    
    @Bean
    public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configure ->
                configure
                    // Definição das regras de autorização
                    // ...
        );
        
        // Configuração de autenticação básica HTTP
        http.httpBasic();
        
        // Configuração de login por formulário
        http.formLogin(Customizer.withDefaults());
        
        // Integração com o serviço de usuários
        http.userDetailsService(userDetailsService);
        
        return http.build();
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
```

### 5. Ordem de Precedência das Regras de Autorização

As regras de autorização no Spring Security continuam sendo avaliadas na **ordem em que são definidas** no método `authorizeHttpRequests()`. A primeira regra que corresponde à requisição determina se o acesso será concedido ou negado.

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

Este endpoint exige que o usuário tenha **ambas** as authorities `ROLE_MANAGER` e `READ_REPORT`.

## Estrutura de Controle de Acesso

### Authorities Implementadas (AuthorityEnum)

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

## Como Configurar o Banco de Dados

Para configurar os usuários no banco de dados, você pode criar um componente de inicialização:

```java
@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        // Cria usuário com role EMPLOYEE
        UserEntity john = new UserEntity();
        john.setName("john");
        john.setPassword("j123456");
        john.createAuthorities(
            AuthorityEnum.ROLE_EMPLOYEE,
            AuthorityEnum.READ_EMPLOYEE
        );
        
        // Cria usuário com role MANAGER
        UserEntity mary = new UserEntity();
        mary.setName("mary");
        mary.setPassword("m123456");
        mary.createAuthorities(
            AuthorityEnum.ROLE_MANAGER,
            AuthorityEnum.READ_EMPLOYEE,
            AuthorityEnum.CREATE_EMPLOYEE,
            AuthorityEnum.UPDATE_EMPLOYEE,
            AuthorityEnum.READ_REPORT
        );
        
        // Cria usuário com role ADMIN
        UserEntity susan = new UserEntity();
        susan.setName("susan");
        susan.setPassword("s123456");
        susan.createAuthorities(
            AuthorityEnum.ROLE_ADMIN,
            AuthorityEnum.READ_EMPLOYEE,
            AuthorityEnum.CREATE_EMPLOYEE,
            AuthorityEnum.UPDATE_EMPLOYEE,
            AuthorityEnum.DELETE_EMPLOYEE,
            AuthorityEnum.READ_REPORT,
            AuthorityEnum.CREATE_REPORT,
            AuthorityEnum.UPDATE_REPORT,
            AuthorityEnum.DELETE_REPORT
        );
        
        // Salva usuários no banco de dados
        userRepository.saveAll(List.of(john, mary, susan));
    }
}
```

## Como Testar

Para testar as diferentes permissões de acesso:

1. Inicie a aplicação
2. O banco de dados (H2 Console) estará disponível em `/h2-console`
3. Teste os endpoints com diferentes usuários:

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

4. Para autenticar, use:
   - Formulário de login padrão do Spring Security
   - Autenticação Básica HTTP com as credenciais apropriadas

## Vantagens desta Abordagem

1. **Persistência**: Usuários e authorities são persistidos em banco de dados.
2. **Flexibilidade**: Fácil adicionar, remover ou modificar usuários e suas permissões.
3. **Centralização**: Todo o controle de acesso é definido no `SecurityConfig.java`.
4. **Controle granular**: Authorities permitem definir permissões específicas para operações individuais.
5. **Consistência interna**: O uso do enum `AuthorityEnum` garante consistência nas authorities.
6. **Expressividade**: As regras de segurança expressam claramente a intenção.

## Pontos Importantes

- O uso de `NoOpPasswordEncoder` é deprecado e recomendado apenas para demonstrações. Em produção, use BCryptPasswordEncoder.
- Em ambientes de produção:
  - Use encoder de senha mais robusto (BCryptPasswordEncoder está comentado no código).
  - Considere implementar mecanismos de cache para usuários e authorities.
  - Habilite CSRF e use HTTPS.
  - Implemente revogação de tokens e controle de sessão.

## Recursos Adicionais

- [Documentação do Spring Security sobre Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Diferença entre Roles e Authorities no Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#appendix-faq-role-vs-authority)
- [Configuração de Persistência no Spring Security](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html)
