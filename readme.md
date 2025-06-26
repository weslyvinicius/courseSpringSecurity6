# Spring Security 6 com Spring Boot 3 - Autenticação JWT com OAuth2 Resource Server

Este projeto demonstra uma implementação moderna do Spring Security 6 com Spring Boot 3, utilizando **OAuth2 Resource Server** para autenticação JWT com chaves RSA, proporcionando um controle de acesso seguro e escalável com persistência em banco de dados.

## Arquitetura da Solução

### Diferencial: OAuth2 Resource Server

Este projeto utiliza a abordagem **OAuth2 Resource Server** do Spring Security, que oferece vantagens significativas sobre implementações manuais:

- **Integração Nativa**: Utiliza os componentes oficiais do Spring Security para JWT
- **Validação Automática**: O framework gerencia toda a validação de tokens
- **Configuração Simplificada**: Menos código customizado, mais padrões
- **Manutenibilidade**: Segue as melhores práticas recomendadas pelo Spring Team
- **Performance**: Otimizações internas do framework

## Estrutura do Projeto

### Configuração e Segurança
- `SecurityConfig.java`: Configuração OAuth2 Resource Server com JWT
- `JwtConfig.java`: Configuração de JwtEncoder/JwtDecoder com chaves RSA
- `AuthenticationManagerConfig.java`: Configuração do AuthenticationManager
- `ConflictException.java`: Exceção customizada para conflitos de usuário

### Serviços de Autenticação
- `AuthService.java`: Serviço de autenticação e registro de usuários
- `JwtService.java`: Serviço para geração de tokens usando Spring Security OAuth2
- `UserDetailsServiceImpl.java`: Serviço para carregamento de usuários do banco de dados

### Controladores
- `AuthController.java`: API REST para login, registro e refresh de tokens
- `EmployeesController.java`: API REST para funcionários com anotações de segurança
- `AdminController.java`: API REST para administradores usando `@PreAuthorize`
- `ReportsController.java`: API REST para relatórios com combinação de roles e authorities

### Entidades e Repositórios
- `UserEntity.java`: Entidade JPA que implementa UserDetails
- `UserAuthorities.java`: Entidade JPA que implementa GrantedAuthority
- `AuthorityEnum.java`: Enumeração com todas as authorities disponíveis
- `UserRepository.java`: Interface para persistência de usuários

## Principais Conceitos Implementados

### 1. OAuth2 Resource Server com JWT

A configuração utiliza o OAuth2 Resource Server do Spring Security para validação automática de tokens:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .anyRequest().authenticated()
        );

        // OAuth2 Resource Server para validação de JWT
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        // Política STATELESS
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

### 2. Configuração de JwtEncoder e JwtDecoder

O `JwtConfig` configura os componentes oficiais do Spring Security para JWT:

```java
@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey();
        RSAPrivateKey privateKey = loadPrivateKey();

        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
}
```

### 3. Serviço JWT Modernizado

O `JwtService` utiliza `JwtEncoder` e `JwtDecoder` oficiais:

```java
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .claim("type", "access")
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String username = jwt.getSubject();
            return username.equals(userDetails.getUsername()) && !isTokenExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 4. Conversor de Authorities JWT

Configura como as authorities do JWT são convertidas para o Spring Security:

```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    
    // Remove prefixos automáticos
    authoritiesConverter.setAuthorityPrefix("");
    
    // Define a claim que contém as authorities
    authoritiesConverter.setAuthoritiesClaimName("authorities");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    
    return jwtConverter;
}
```

### 5. Sistema de Access e Refresh Tokens

O projeto implementa um sistema robusto de renovação de tokens:

#### Duração dos Tokens
- **Access Token**: 1 hora (para operações da API)
- **Refresh Token**: 7 dias (para renovação automática)

#### Fluxo de Renovação
1. Cliente faz login e recebe ambos os tokens
2. Utiliza access token para chamadas à API
3. Quando access token expira, usa refresh token para obter novos tokens
4. Sistema valida refresh token e emite novos access + refresh tokens

```java
public String generateRefreshToken(Authentication authentication) {
    return generateToken(authentication, REFRESH_EXPIRATION_DAYS, ChronoUnit.DAYS);
}

public boolean validateRefreshToken(String token, UserDetails userDetails) {
    try {
        if (!isRefreshToken(token)) {
            return false;
        }
        Jwt jwt = jwtDecoder.decode(token);
        String username = jwt.getSubject();
        return username.equals(userDetails.getUsername()) && !isTokenExpired(jwt);
    } catch (Exception e) {
        return false;
    }
}
```

### 6. AuthenticationManager Configurado

Configuração explícita do AuthenticationManager para o processo de login:

```java
@Configuration
public class AuthenticationManagerConfig {

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        
        return new ProviderManager(authProvider);
    }
}
```

## Vantagens da Arquitetura OAuth2 Resource Server

### 1. **Conformidade com Padrões**
- Segue especificações OAuth2 e OpenID Connect
- Compatível com provedores de identidade externos
- Interoperabilidade com outros sistemas

### 2. **Segurança Aprimorada**
- Validação automática de tokens pelo framework
- Suporte nativo a diferentes algoritmos de assinatura
- Tratamento robusto de exceções de segurança

### 3. **Manutenibilidade**
- Menos código customizado para manter
- Atualizações automáticas de segurança via Spring
- Configuração declarativa vs imperativa

### 4. **Performance**
- Otimizações internas do Spring Security
- Cache automático de chaves públicas
- Processamento eficiente de tokens

### 5. **Extensibilidade**
- Fácil integração com diferentes provedores JWT
- Suporte a múltiplas fontes de chaves
- Personalização via conversores e validadores

## Estrutura de Resposta de Autenticação

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## Como Configurar as Chaves RSA

### Geração das Chaves

```bash
# Gerar chave privada RSA
openssl genpkey -algorithm RSA -out private.pem -pkcs8

# Extrair chave pública
openssl rsa -pubout -in private.pem -out public.pem
```

### Localização dos Arquivos
Coloque os arquivos na pasta `src/main/resources/`:
```
src/
└── main/
    └── resources/
        ├── private.pem
        └── public.pem
```

## Testando a API

### 1. Registro de Usuário

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

### 3. Renovação de Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
  }'
```

### 4. Acesso a Recursos Protegidos

```bash
curl -X GET http://localhost:8080/api/employees/123 \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..."
```

## Controle de Acesso com @PreAuthorize

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {
    
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public String getEmployee(@PathVariable String employeeId) {
        return "Employee: " + employeeId;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_EMPLOYEE')")
    public String createEmployee() {
        return "Employee created";
    }
}
```

## Sistema de Authorities

### Authorities Implementadas

**Funcionários:**
- `READ_EMPLOYEE`: Leitura de dados de funcionários
- `CREATE_EMPLOYEE`: Criação de funcionários
- `UPDATE_EMPLOYEE`: Atualização de funcionários
- `DELETE_EMPLOYEE`: Exclusão de funcionários

**Relatórios:**
- `READ_REPORT`: Leitura de relatórios
- `CREATE_REPORT`: Criação de relatórios
- `UPDATE_REPORT`: Atualização de relatórios
- `DELETE_REPORT`: Exclusão de relatórios

**Roles (como authorities):**
- `ROLE_EMPLOYEE`: Papel de funcionário comum
- `ROLE_MANAGER`: Papel de gerente
- `ROLE_ADMIN`: Papel de administrador

## Matriz de Permissões

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
| `/api/admin/**` | Todos | ROLE_ADMIN | susan |


## Considerações de Segurança

### Configuração de Produção
- Use chaves RSA de pelo menos 2048 bits
- Sempre utilize HTTPS em produção
- Configure CORS adequadamente para sua aplicação
- Implemente rate limiting nos endpoints de autenticação

### Armazenamento de Tokens
- **Frontend Web**: Use httpOnly cookies para refresh tokens
- **Mobile/SPA**: Armazene em storage seguro (não localStorage)
- **Server-to-Server**: Use mTLS ou assinatura de requests

### Monitoramento
- Registre tentativas de autenticação falhadas
- Monitore uso de tokens expirados
- Implemente alertas para comportamentos suspeitos
- Considere implementar token blacklist para revogação

### Rotação de Chaves
- Implemente rotação periódica de chaves RSA
- Mantenha múltiplas chaves para transição suave
- Use JWK Set URL para distribuição automática de chaves

## Troubleshooting

### Problemas Comuns

1. **Token não reconhecido**
   - Verifique se as chaves RSA estão corretas
   - Confirme se o formato PEM está adequado
   - Valide se o issuer está configurado corretamente

2. **Authorities não funcionando**
   - Verifique a configuração do `JwtAuthenticationConverter`
   - Confirme se a claim "authorities" está presente no token
   - Valide se o prefixo de authorities está correto

3. **Refresh token inválido**
   - Confirme se o token tem o tipo "refresh"
   - Verifique se não está expirado
   - Valide se o usuário ainda existe e está ativo

## Recursos Adicionais

- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT.io - Debugger de Tokens](https://jwt.io/)
- [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
- [Spring Security Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)