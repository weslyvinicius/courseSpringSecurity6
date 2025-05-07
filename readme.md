# Spring Security 6 com Spring Boot 3 - Autenticação em Memória

Este projeto demonstra a implementação do Spring Security 6 com Spring Boot 3, utilizando autenticação básica (HTTP Basic), autenticação por formulário (Form Login) e gerenciamento de usuários em memória.

## Estrutura do Projeto

O projeto possui uma estrutura simples para demonstrar os conceitos básicos de segurança:

- `SecurityConfig.java`: Configuração de segurança da aplicação e definição dos usuários em memória
- `MyApiController.java`: Controlador REST com endpoints protegidos e públicos
- `application.yml`: Configurações da aplicação

## Principais Conceitos Demonstrados

### 1. Configuração de Segurança

No arquivo `SecurityConfig.java`, temos a configuração do Spring Security:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET,"/myfree").permitAll()
            .requestMatchers("/logout").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin(Customizer.withDefaults())
            .httpBasic()
            .and()
            .cors();

        return http.build();
    }
}
```

Nesta configuração:

- O CSRF está desabilitado (`.csrf().disable()`)
- Definimos regras de autorização:
  - O endpoint `/myfree` é público (permitido para todos)
  - O endpoint `/logout` é público
  - Todos os outros endpoints requerem autenticação
- Habilitamos autenticação por formulário com configurações padrão
- Habilitamos autenticação básica HTTP
- Configuramos CORS

### 2. Gerenciamento de Usuários em Memória

O projeto demonstra três abordagens para criar usuários em memória, com a primeira sendo a implementação ativa:

#### a) Utilizando Default Password Encoder

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

#### b) Utilizando Texto Plano (Comentado no código)

```java
@Bean
public InMemoryUserDetailsManager userDetailsManager(){
    UserDetails john = User.builder()
                       .username("john")
                       .password("{noop}j123456")
                       .roles("EMPLOYEE")
                       .build();
    
    // ...outros usuários...
    
    return new InMemoryUserDetailsManager(john, mary, susan);
}
```

#### c) Utilizando BCrypt (Comentado no código)

```java
@Bean
public InMemoryUserDetailsManager userDetailsManager(){
    UserDetails john = User.builder()
                        .username("john")
                        .password("{bcrypt}$2a$12$...")
                        .roles("EMPLOYEE")
                        .build();
    
    // ...outros usuários...
    
    return new InMemoryUserDetailsManager(john, mary, susan);
}
```

### 3. Hierarquia de Roles (Papéis)

O projeto implementa uma estrutura hierárquica de papéis:

- `EMPLOYEE`: Nível básico de acesso (john)
- `MANAGER`: Nível intermediário, também inclui permissões de EMPLOYEE (mary)
- `ADMIN`: Nível mais alto, inclui permissões de MANAGER e EMPLOYEE (susan)

### 4. Endpoints da API

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
2. Acesse `http://localhost:8080/myfree` - Deve funcionar sem autenticação
3. Acesse `http://localhost:8080/authenticate` - Será redirecionado para o login
   - Use as credenciais de um dos usuários configurados:
     - john/j123456 (ROLE_EMPLOYEE)
     - mary/m123456 (ROLE_MANAGER, ROLE_EMPLOYEE)
     - susan/s123456 (ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE)

## Pontos Importantes

- A autenticação em memória é útil para testes e aplicações simples, mas não é recomendada para produção
- O método `withDefaultPasswordEncoder()` é marcado como deprecated e deve ser usado apenas para demonstrações
- Em ambientes de produção, o CSRF deve ser habilitado
- Para aplicações reais, considere implementar autenticação com banco de dados ou serviços de identidade

## Recursos Adicionais

- [Documentação do Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Security 6 - Novas Funcionalidades](https://spring.io/blog/2022/02/21/spring-security-5-7-0-m2-and-5-6-2-available-now)
- [Spring Boot Security Starter](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-security)
