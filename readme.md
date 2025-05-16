# Estudo de Autenticação Customizada no Spring Security

Este projeto demonstra a implementação de um mecanismo de autenticação customizado no Spring Security, utilizando um sistema baseado em chave de API através do header da requisição.

## Visão Geral da Arquitetura

O projeto implementa um fluxo de autenticação customizado no Spring Security através dos seguintes componentes:

1. **CustomAuthentication**: Uma implementação da interface `Authentication` do Spring Security que armazena o estado de autenticação e a chave de API.

2. **CustomAuthenticationFilter**: Um filtro que intercepta todas as requisições HTTP, extrai a chave de API do header e inicia o processo de autenticação.

3. **CustomAuthenticationManager**: Gerencia o processo de autenticação, delegando para o provider adequado.

4. **CustomAuthenticationProvider**: Implementa a lógica de autenticação, comparando a chave de API recebida com a chave secreta configurada.

5. **SecurityConfig**: Configuração do Spring Security que registra o filtro customizado.

## Fluxo de Autenticação

1. Uma requisição HTTP chega ao servidor
2. O `CustomAuthenticationFilter` intercepta a requisição e extrai a chave do header "key"
3. O filtro cria um objeto `CustomAuthentication` não autenticado com a chave extraída
4. O filtro delega a autenticação para o `CustomAuthenticationManager`
5. O manager verifica se existe um provider que suporta o tipo de autenticação
6. O `CustomAuthenticationProvider` verifica se a chave recebida corresponde à chave secreta
7. Se a autenticação for bem-sucedida, o filtro estabelece o contexto de segurança
8. A requisição continua para o próximo filtro na cadeia

## Componentes Principais

### CustomAuthentication

```java
@AllArgsConstructor
@Data
public class CustomAuthentication implements Authentication {
    private final boolean authentication;
    private final String key;
    
    // Implementação dos métodos da interface Authentication
}
```

Esta classe implementa a interface `Authentication` do Spring Security, armazenando:
- Um flag indicando se a autenticação foi bem-sucedida
- A chave de API fornecida na requisição

### CustomAuthenticationFilter

```java
@Component
@AllArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final CustomAuthenticationManager customAuthenticationManager;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String key = String.valueOf(request.getHeader("key"));
        CustomAuthentication ca = new CustomAuthentication(false, key);
        
        var a = customAuthenticationManager.authenticate(ca);
        
        if (a.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(a);
            filterChain.doFilter(request, response);
        }
    }
}
```

Este filtro:
1. Extrai a chave do header da requisição
2. Cria um objeto de autenticação não autenticado
3. Delega a autenticação para o manager
4. Se a autenticação for bem-sucedida, estabelece o contexto de segurança e passa a requisição para os próximos filtros

### CustomAuthenticationManager

```java
@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {
    private final CustomAuthenticationProvider provider;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (provider.supports(authentication.getClass())) {
            return provider.authenticate(authentication);
        }
        
        throw new BadCredentialsException("Oh No!");
    }
}
```

O manager:
1. Verifica se existe um provider adequado para processar a autenticação
2. Delega a autenticação para o provider correspondente
3. Lança uma exceção caso a autenticação falhe

### CustomAuthenticationProvider

```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Value("${our.very.very.very.secret.key}")
    private String key;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthentication ca = (CustomAuthentication) authentication;
        String headerKey = ca.getKey();
        
        if (key.equals(headerKey)) {
            return new CustomAuthentication(true, null);
        }
        
        throw new BadCredentialsException("Oh No!");
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthentication.class.equals(authentication);
    }
}
```

O provider:
1. Recebe um objeto de autenticação
2. Compara a chave fornecida com a chave secreta configurada
3. Retorna um objeto de autenticação autenticado se as chaves coincidirem
4. Lança uma exceção caso as chaves não coincidam

### SecurityConfig

```java
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

A configuração do Spring Security registra o filtro customizado na cadeia de filtros.

## Mapa de Sequência das Classes em Execução

```
┌──────────────────────┐      ┌───────────────────────────┐      ┌─────────────────────────────┐
│                      │      │                           │      │                             │
│    HTTP Request      │─────▶│ CustomAuthenticationFilter │─────▶│ CustomAuthenticationManager │
│  (with "key" header) │      │                           │      │                             │
│                      │      └───────────────────────────┘      └─────────────────────────────┘
└──────────────────────┘                │                                       │
                                        │                                       │
                                        │                                       ▼
                                        │                        ┌─────────────────────────────┐
                                        │                        │                             │
                                        │                        │ CustomAuthenticationProvider│
                                        │                        │                             │
                                        │                        └─────────────────────────────┘
                                        │                                       │
                                        │                                       │
                                        ▼                                       ▼
┌──────────────────────┐      ┌───────────────────────────┐      ┌─────────────────────────────┐
│                      │      │                           │      │                             │
│  Protected Resource  │◀─────│    SecurityContextHolder  │◀─────│    CustomAuthentication     │
│                      │      │                           │      │    (authenticated=true)     │
└──────────────────────┘      └───────────────────────────┘      └─────────────────────────────┘
```

## Resumo do Fluxo

1. A requisição HTTP chega com o header "key"
2. `CustomAuthenticationFilter` extrai a chave e cria um objeto `CustomAuthentication(false, key)`
3. O filtro passa o objeto para `CustomAuthenticationManager`
4. O manager verifica se `CustomAuthenticationProvider` suporta esse tipo de autenticação
5. O provider compara a chave recebida com a chave configurada
6. Se as chaves coincidirem, retorna `CustomAuthentication(true, null)`
7. O filtro estabelece o contexto de segurança com a autenticação bem-sucedida
8. A requisição continua para o recurso protegido

## Considerações Finais

Esta implementação demonstra como criar um mecanismo de autenticação customizado no Spring Security, substituindo a autenticação tradicional baseada em usuário e senha por uma autenticação baseada em chave de API. Este padrão é comumente utilizado em APIs REST e serviços web onde a autenticação baseada em tokens é mais adequada.

## Pontos Importantes

- A chave secreta é configurada via propriedade `our.very.very.very.secret.key`
- O filtro não permite que a requisição continue se a autenticação falhar
- A implementação é minimalista, focando apenas nos componentes essenciais para o fluxo de autenticação
- Em um ambiente de produção, seria recomendável adicionar mais camadas de segurança, como HTTPS, expiração de tokens, etc.
