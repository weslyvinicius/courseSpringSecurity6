# Spring Security 6 com Spring Boot 3 - Autenticação Básica e Form Login

Este projeto demonstra a implementação do Spring Security 6 com Spring Boot 3, utilizando autenticação básica (HTTP Basic) e autenticação por formulário (Form Login).

## Estrutura do Projeto

O projeto possui uma estrutura simples para demonstrar os conceitos básicos de segurança:

- `SecurityConfig.java`: Configuração de segurança da aplicação
- `MyApiController.java`: Controlador REST com endpoints protegidos e públicos
- `application.yml`: Configurações da aplicação, incluindo credenciais padrão

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

### 2. Endpoints da API

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

### 3. Configuração de Usuário

No arquivo `application.yml`, configuramos um usuário padrão:

```yaml
spring:
  security:
    user:
      name: batman
      password: 123456
```

## Como Testar

1. Inicie a aplicação
2. Acesse `http://localhost:8080/myfree` - Deve funcionar sem autenticação
3. Acesse `http://localhost:8080/authenticate` - Será redirecionado para o login
   - Para autenticação via formulário: Use o formulário de login padrão
   - Para autenticação básica: Envie cabeçalho `Authorization: Basic YmF0bWFuOjEyMzQ1Ng==` (batman:123456 em Base64)

## Pontos Importantes

- A autenticação básica HTTP não é segura para produção sem HTTPS
- Em ambientes de produção, o CSRF deve ser habilitado
- As senhas devem ser armazenadas com hash em vez de texto plano
- Considere usar um mecanismo de autenticação mais robusto para aplicações em produção

## Recursos Adicionais

- [Documentação do Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Security 6 - Novas Funcionalidades](https://spring.io/blog/2022/02/21/spring-security-5-7-0-m2-and-5-6-2-available-now)
- [Spring Boot Security Starter](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-security)
