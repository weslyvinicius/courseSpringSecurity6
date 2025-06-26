# Spring Security 6 com Spring Boot 3 - Autenticação JWT com RSA

Este projeto demonstra uma implementação avançada do Spring Security 6 com Spring Boot 3, utilizando autenticação JWT com chaves RSA para um controle de acesso seguro e moderno, com persistência em banco de dados e anotações `@PreAuthorize`.

## Estrutura do Projeto

O projeto implementa um sistema de autenticação JWT completo baseado em authorities e roles com persistência:

### Configuração e Segurança
- `SecurityConfig.java`: Configuração centralizada de segurança com JWT
- `JwtAuthenticationFilter.java`: Filtro para validação de tokens JWT
- `JwtConfig.java`: Configuração para carregamento de chaves RSA
- `ConflictException.java`: Exceção customizada para conflitos de usuário

### Serviços de Autenticação
- `AuthService.java`: Serviço de autenticação e registro de usuários
- `JwtService.java`: Serviço para geração e validação de tokens JWT
- `UserDetailsServiceImpl.java`: Serviço para carregamento de usuários do banco de dados

### Controladores
- `AuthController.java`: API REST para login e registro
- `EmployeesController.java`: API REST para funcionários com anotações de segurança
- `AdminController.java`: API REST para administradores usando `@PreAuthorize` em nível de classe
- `ReportsController.java`: API REST para relatórios com exemplos de combinação de roles e authorities

### Entidades e Repositórios
- `UserEntity.java`: Entidade JPA que implementa UserDetails para autenticação
- `UserAuthorities.java`: Entidade JPA que implementa GrantedAuthority para autorização
- `AuthorityEnum.java`: Enumeração com todas as authorities disponíveis
- `UserRepository.java`: Interface para persistência de usuários

## Principais Conceitos Demonstrados

### 1. Autenticação JWT com Chaves RSA

O projeto utiliza chaves RSA para assinar e validar tokens JWT, proporcionando maior segurança:

```java
@Component
public class JwtConfig {
   @Value("classpath:private.pem")
   private Resource privateKeyResource;

   @Value("classpath:public.pem")
   private Resource publicKeyResource;

   public RSAPublicKey loadPublicKey() throws Exception {
      // Carrega chave pública para validação de tokens
   }

   public RSAPrivateKey loadPrivateKey() throws Exception {
      // Carrega chave privada para assinatura de tokens
   }
}
```

### 2. Filtro de Autenticação JWT

O `JwtAuthenticationFilter` intercepta todas as requisições e valida tokens JWT:

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
   private final JwtService jwtService;
   private final UserDetailsService userDetailsService;

   @Override
   protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
      // Extrai token do header Authorization
      String authHeader = request.getHeader("Authorization");

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
         String jwt = authHeader.substring(7);
         String username = jwtService.getUsernameFromToken(jwt);

         // Valida token e define autenticação no SecurityContext
         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(jwt, userDetails)) {
               // Define autenticação no contexto de segurança
            }
         }
      }

      filterChain.doFilter(request, response);
   }
}
```

### 3. Configuração de Segurança Stateless

A configuração de segurança é otimizada para APIs REST stateless:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
   private final JwtAuthenticationFilter jwtAuthenticationFilter;

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
              // Desabilita CSRF (stateless com JWT)
              .csrf(csrf -> csrf.disable())
              .cors(cors -> cors.disable())
              // Define sessão como stateless
              .sessionManagement(session ->
                      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              // Configura endpoints públicos e protegidos
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                      .anyRequest().authenticated())
              // Adiciona filtro JWT antes do filtro padrão
              .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }
}
```

### 4. Serviço de Autenticação com Duas Abordagens

O `AuthService` demonstra duas abordagens para autenticação:

#### Abordagem 1: AuthenticationManager (Recomendada)
```java
public String authenticate(String username, String password) {
   try {
      // Spring Security faz tudo automaticamente:
      // 1. Carrega usuário via UserDetailsService
      // 2. Valida senha com PasswordEncoder
      // 3. Verifica conta (bloqueada/expirada)
      // 4. Popula authorities
      Authentication auth = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(username, password));

      return jwtService.generateToken(auth);
   } catch (BadCredentialsException e) {
      throw new BadCredentialsException("Credenciais inválidas");
   }
}
```

#### Abordagem 2: Validação Manual
```java
public String authenticateManual(String username, String password) {
   try {
      UserDetails user = userDetailsService.loadUserByUsername(username);

      // Validações manuais:
      if (!passwordEncoder.matches(password, user.getPassword())) {
         throw new RuntimeException("Senha inválida");
      }

      if (!user.isEnabled()) {
         throw new RuntimeException("Conta desabilitada");
      }

      // Gera token com authorities
      List<String> authorities = user.getAuthorities()
              .stream()
              .map(GrantedAuthority::getAuthority)
              .toList();

      return jwtService.generateTokenForUser(user.getUsername(), authorities);
   } catch (Exception e) {
      throw new RuntimeException(e);
   }
}
```

### 5. Geração e Validação de Tokens JWT

O `JwtService` utiliza a biblioteca JJWT moderna para trabalhar com tokens:

```java
@Service
@RequiredArgsConstructor
public class JwtService {
   private final JwtConfig jwtConfig;
   private final long JWT_EXPIRATION_HOURS = 3;

   public String generateToken(Authentication authentication) throws Exception {
      Instant now = Instant.now();
      List<String> authorities = authentication.getAuthorities()
              .stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

      return Jwts.builder()
              .subject(authentication.getName())
              .claim("authorities", authorities)
              .issuedAt(Date.from(now))
              .expiration(Date.from(now.plus(JWT_EXPIRATION_HOURS, ChronoUnit.HOURS)))
              .signWith(jwtConfig.loadPrivateKey(), Jwts.SIG.RS256)
              .compact();
   }

   public boolean validateToken(String token, UserDetails userDetails) {
      try {
         Claims claims = getClaimsFromToken(token);
         String username = claims.getSubject();
         return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
      } catch (Exception e) {
         return false;
      }
   }
}
```

### 6. API de Autenticação

Endpoints para login, registro e renovação de tokens JWT:

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest authRequest) {
        TokenResponse tokenResponse = authService.authenticate(
            authRequest.username(),
            authRequest.password());
        
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest registerRequest) {
        TokenResponse tokenResponse = authService.register(
            registerRequest.username(),
            registerRequest.password());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(tokenResponse);
    }
}
```

#### Resposta de Autenticação (TokenResponse)

A resposta de autenticação inclui tanto o access token quanto o refresh token:

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "tokenType": "Bearer",
  "expiresIn": 10800
}
```

### 7. Sistema de Refresh Token

O projeto implementa um sistema completo de refresh tokens para maior segurança e experiência do usuário:

#### Fluxo de Autenticação com Refresh Token

1. **Login Inicial**:
   - Usuário faz login com credenciais (username/password)
   - Sistema valida credenciais e retorna dois tokens:
      - **Access Token**: Token de curta duração (3 horas) para acesso aos recursos
      - **Refresh Token**: Token de longa duração (7 dias) para renovação de acesso

2. **Uso do Access Token**:
   - Cliente utiliza o access token no header `Authorization: Bearer <token>`
   - Token é validado a cada requisição protegida
   - Quando expira, requisições retornam erro 401 (Unauthorized)

3. **Renovação com Refresh Token**:
   - Cliente detecta expiração do access token
   - Envia refresh token para endpoint `/api/auth/refresh`
   - Sistema valida refresh token e retorna novos tokens (access + refresh)
   - Cliente atualiza tokens armazenados

#### Vantagens do Refresh Token

- **Segurança**: Access tokens têm curta duração, limitando janela de comprometimento
- **Experiência do Usuário**: Usuário não precisa fazer login repetidamente
- **Controle de Acesso**: Refresh tokens podem ser revogados centralmente
- **Rotação de Tokens**: Novos tokens são gerados a cada renovação

#### Exemplo de Implementação no Cliente

```javascript
// Interceptor para renovação automática de tokens
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/auth/refresh', { 
          refreshToken 
        });
        
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
        // Retry original request with new token
        error.config.headers.Authorization = `Bearer ${accessToken}`;
        return axios.request(error.config);
      } catch (refreshError) {
        // Redirect to login
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

### 8. Diferença entre Roles e Authorities

No Spring Security:

- **Roles**: Representam um papel ou função de um usuário no sistema (EMPLOYEE, MANAGER, ADMIN). São tratadas internamente como authorities com o prefixo "ROLE_".
- **Authorities**: Representam permissões específicas para ações (READ_EMPLOYEE, CREATE_EMPLOYEE, etc.).

### 9. Controle de Acesso com `@PreAuthorize`

O controle de acesso é definido em cada método dos controladores usando anotações `@PreAuthorize`:

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public String getEmployee(@PathVariable String employeeId) {
        return "Read Employee: " + employeeId;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_EMPLOYEE')")
    public String createEmployee() {
        return "Employee created successfully";
    }
}
```

### 10. Combinando Roles e Authorities

Exemplo de endpoint que requer tanto authority quanto role:

```java
@GetMapping("/combined-auth")
@PreAuthorize("hasAuthority('READ_REPORT') and hasRole('MANAGER')")
public String getReportWithCombinedAuth() {
    return "This endpoint requires both READ_REPORT authority and MANAGER role";
}
```

## Configuração de Chaves RSA

Para gerar as chaves RSA necessárias:

```bash
# Gerar chave privada
openssl genpkey -algorithm RSA -out private.pem -pkcs8 -pass pass:mypassword

# Gerar chave pública
openssl rsa -pubout -in private.pem -out public.pem
```

Coloque os arquivos `private.pem` e `public.pem` na pasta `src/main/resources/`.

## Estrutura de Authorities

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

## Como Testar a API

### 1. Registro de Usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "tokenType": "Bearer",
  "expiresIn": 10800
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi...",
  "tokenType": "Bearer",
  "expiresIn": 10800
}
```

### 3. Renovação de Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi..."
  }'
```

### 4. Acesso a Endpoints Protegidos

```bash
curl -X GET http://localhost:8080/api/employees/123 \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi..."
```

**Tratamento de Token Expirado:**
```bash
# Se o access token expirou, use o refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwi..."
  }'
```

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


## Vantagens da Abordagem JWT com Refresh Token

1. **Stateless**: Não requer armazenamento de sessão no servidor
2. **Escalabilidade**: Ideal para aplicações distribuídas e microserviços
3. **Segurança Aprimorada**:
   - Access tokens de curta duração limitam janela de comprometimento
   - Refresh tokens permitem revogação centralizada
   - Rotação automática de tokens
4. **Performance**: Validação local sem consulta ao banco de dados
5. **Flexibilidade**: Tokens podem ser validados por diferentes serviços
6. **Experiência do Usuário**: Renovação transparente sem necessidade de novo login
7. **Padrão da Indústria**: JWT é amplamente aceito e suportado

## Considerações de Segurança

- **Expiração de Tokens**:
   - Access tokens têm validade de 3 horas (configurável)
   - Refresh tokens têm validade de 7 dias (configurável)
- **Chaves RSA**: Use chaves de pelo menos 2048 bits em produção
- **HTTPS**: Sempre use HTTPS em produção para proteger tokens
- **Armazenamento Seguro**:
   - Use httpOnly cookies para refresh tokens em aplicações web
   - Evite localStorage para tokens sensíveis
- **Revogação de Tokens**: Implemente blacklist ou whitelist para tokens revogados
- **Rotação de Chaves**: Implemente rotação periódica de chaves RSA
- **Rate Limiting**: Implemente limitação de tentativas no endpoint de refresh
- **Logging e Monitoramento**: Registre tentativas de uso de tokens inválidos

## Estrutura de Resposta de Erro

```json
{
   "timestamp": "2024-01-15T10:30:00",
   "status": 401,
   "error": "Unauthorized",
   "message": "Token JWT inválido ou expirado",
   "path": "/api/employees/123"
}
```

## Recursos Adicionais

- [JWT.io - Debugger de Tokens JWT](https://jwt.io/)
- [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
- [Spring Security JWT Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [JJWT Library Documentation](https://github.com/jwtk/jjwt)