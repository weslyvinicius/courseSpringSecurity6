# Spring Security 6 com Spring Boot 3 - Autenticação com Banco de Dados

Este projeto demonstra a implementação do Spring Security 6 com Spring Boot 3, utilizando autenticação básica (HTTP Basic), autenticação por formulário (Form Login) e gerenciamento de usuários em banco de dados.

## Estrutura do Projeto

O projeto possui uma estrutura que implementa autenticação com banco de dados:

- `SecurityConfig.java`: Configuração de segurança da aplicação
- `UserDetailsServiceImpl.java`: Implementação do serviço que carrega usuários do banco de dados
- `UserEntity.java`: Entidade JPA que implementa UserDetails
- `UserRepository.java`: Repositório JPA para acesso aos dados dos usuários
- `MyApiController.java`: Controlador REST com endpoints protegidos e públicos

## Principais Conceitos Demonstrados

### 1. Configuração de Segurança

No arquivo `SecurityConfig.java`, temos a configuração do Spring Security:

```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET,"/myfree").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/h2-console").permitAll()
            .requestMatchers("/logout").permitAll()
            .anyRequest().authenticated()
            .and()
            .userDetailsService(userDetailsService)
            .formLogin(Customizer.withDefaults())
            .httpBasic()
            .and()
            .cors();

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}
```

Nesta configuração:

- O CSRF está desabilitado (`.csrf().disable()`)
- Definimos regras de autorização:
  - O endpoint `/myfree` é público (permitido para todos)
  - Endpoints de console H2 (`/h2-console/**` e `/h2-console`) são públicos
  - O endpoint `/logout` é público
  - Todos os outros endpoints requerem autenticação
- Configuramos o serviço de detalhes de usuário personalizado (`userDetailsService`)
- Habilitamos autenticação por formulário com configurações padrão
- Habilitamos autenticação básica HTTP
- Configuramos CORS
- Utilizamos `NoOpPasswordEncoder` (senha em texto plano, não recomendado para produção)

### 2. Autenticação com Banco de Dados

O projeto implementa autenticação baseada em banco de dados através de:

#### a) UserDetailsServiceImpl

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

Esta classe:
- Implementa a interface `UserDetailsService` do Spring Security
- Carrega usuários do banco de dados usando o `UserRepository`
- Lança exceção quando o usuário não for encontrado

#### b) UserEntity

```java
@Entity
@Table(name = "customer_user")
@Data
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String password;

    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // Outros métodos da interface UserDetails implementados...
}
```

Esta entidade:
- Representa um usuário no banco de dados
- Implementa a interface `UserDetails` do Spring Security
- Define campos para ID, nome e senha
- Retorna `null` para autoridades (sem implementação de perfis/roles)

#### c) UserRepository

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByName(String name);
}
```

Este repositório:
- Estende JpaRepository para operações CRUD básicas
- Define um método para buscar usuários pelo nome

### 3. Endpoints da API

No arquivo `MyApiController.java`, temos dois endpoints:

```java
@RestController
public class MyApiController {

    @GetMapping("/myfree")
    public String myFree(){
        return "Acessando my endpoint free";
    }

    @GetMapping("/authenticate")
    public String myAuthenticate(){
        return "Acessando my endpoint authenticate";
    }
}
```

- `/myfree`: Endpoint público, acessível sem autenticação
- `/authenticate`: Endpoint protegido, requer autenticação

## Como Testar

1. Inicie a aplicação
2. Acesse o console H2 em `http://localhost:8080/h2-console` para verificar ou adicionar usuários
3. Acesse `http://localhost:8080/myfree` - Deve funcionar sem autenticação
4. Acesse `http://localhost:8080/authenticate` - Será redirecionado para o login
   - Use as credenciais de um usuário cadastrado no banco de dados

## Pontos Importantes

- A configuração atual utiliza `NoOpPasswordEncoder` (senha em texto plano), o que não é seguro para ambientes de produção
- Há comentários no código para implementar `BCryptPasswordEncoder`, que seria uma opção mais segura
- Em ambientes de produção, o CSRF deve ser habilitado
- A implementação atual de `UserEntity` retorna `null` para `getAuthorities()`, o que significa que não há implementação de roles/perfis
- O console H2 está habilitado e acessível publicamente, o que deve ser desabilitado em produção

## Melhorias Recomendadas

1. Implementar BCryptPasswordEncoder para armazenar senhas de forma segura
2. Adicionar roles/perfis através do método `getAuthorities()` na entidade `UserEntity`
3. Habilitar CSRF em produção
4. Desabilitar o console H2 em produção ou protegê-lo adequadamente
5. Implementar testes de segurança

## Recursos Adicionais

- [Documentação do Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Security 6 - Novas Funcionalidades](https://spring.io/blog/2022/02/21/spring-security-5-7-0-m2-and-5-6-2-available-now)
- [Spring Boot Security Starter](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-security)