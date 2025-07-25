# OAuth2 Authorization Code Flow - Aplicação de Estudo

Este projeto demonstra o fluxo **OAuth2 Authorization Code Grant** usando Spring Authorization Server e um cliente OAuth2 personalizado.

## 📋 Estrutura do Projeto

- **Authorization Server** (porta 8080) - Servidor que autentica usuários e emite tokens
- **Client Application** (porta 8081) - Aplicação cliente que precisa acessar recursos protegidos

## 🔄 Como Funciona o Fluxo OAuth2

```
1. Usuário acessa Client App (8081)
2. Client redireciona para Authorization Server (8080/oauth2/authorize)
3. Usuário faz login no Authorization Server
4. Authorization Server redireciona de volta com código de autorização
5. Client troca o código por access token
6. Client pode usar o token para acessar recursos
```

### 📊 Diagrama de Sequência Detalhado

```mermaid
sequenceDiagram
    participant User as 👤 Usuário
    participant Client as 🖥️ Client App<br/>(porta 8081)
    participant AuthServer as 🔐 Authorization Server<br/>(porta 8080)

    Note over User,AuthServer: 🚀 Iniciando o Fluxo OAuth2

    User->>+Client: 1. Acessa http://localhost:8081
    Client->>User: 2. Exibe página inicial com botão "Iniciar Fluxo OAuth2"
    
    User->>Client: 3. Clica em "Iniciar Fluxo OAuth2"
    Note over Client: Constrói URL de autorização:<br/>http://localhost:8080/oauth2/authorize<br/>?response_type=code<br/>&client_id=my-client<br/>&redirect_uri=http://localhost:8081/callback<br/>&scope=openid profile<br/>&state=xyz123
    
    Client->>User: 4. Redireciona para Authorization Server
    User->>+AuthServer: 5. GET /oauth2/authorize (com parâmetros)
    
    Note over AuthServer: Usuário não está autenticado
    AuthServer->>User: 6. Redireciona para /login
    User->>AuthServer: 7. GET /login - Exibe formulário de login
    
    User->>AuthServer: 8. POST /login<br/>user: "user"<br/>password: "password"
    Note over AuthServer: Valida credenciais<br/>e gera authorization code
    
    AuthServer->>User: 9. Redireciona para redirect_uri<br/>http://localhost:8081/callback?code=ABC123&state=xyz123
    User->>+Client: 10. GET /callback?code=ABC123&state=xyz123
    
    Note over Client: 🎯 AUTHORIZATION CODE RECEBIDO!<br/>code = ABC123
    
    Note over Client: Agora vamos trocar o código por tokens
    Client->>+AuthServer: 11. POST /oauth2/token<br/>Authorization: Basic base64(my-client:secret)<br/>grant_type=authorization_code<br/>code=ABC123<br/>redirect_uri=http://localhost:8081/callback
    
    Note over AuthServer: Valida código e credenciais<br/>do cliente
    AuthServer->>-Client: 12. Retorna tokens:<br/>{<br/>"access_token": "eyJ...",<br/>"id_token": "eyJ...",<br/>"refresh_token": "def456",<br/>"token_type": "Bearer",<br/>"expires_in": 3600<br/>}
    
    Client->>-User: 13. Exibe página de resultado com:<br/>✅ Authorization Code<br/>✅ Access Token<br/>✅ ID Token<br/>✅ Refresh Token<br/>✅ Informações do token
    
    Note over User,AuthServer: 🎉 Fluxo OAuth2 Concluído com Sucesso!
    
    rect rgb(240, 248, 255)
        Note over User,AuthServer: 🔄 Próximos Passos (opcional)
        Note over Client: O Client agora pode usar o<br/>access_token para acessar<br/>recursos protegidos em nome do usuário
        Note over Client: O refresh_token pode ser usado<br/>para obter novos access_tokens<br/>sem necessidade de novo login
    end
```

## ⚙️ Configurações

### Authorization Server (porta 8080)
- **Cliente registrado:** `my-client`
- **Client Secret:** `secret`
- **Redirect URI:** `http://localhost:8081/callback`
- **Scopes:** `openid`, `profile`
- **Usuário de teste:** `user` / `password`

### Client Application (porta 8081)
- **Client ID:** `my-client`
- **Client Secret:** `secret`
- **Authorization Server:** `http://localhost:8080`

## 🚀 Como Executar

### 1. Iniciar o Authorization Server
```bash
# No terminal 1 - rode a aplicação na porta 8080
mvn spring-boot:run -Dserver.port=8080
```

### 2. Iniciar a Client Application
```bash
# No terminal 2 - rode a aplicação na porta 8081
mvn spring-boot:run -Dserver.port=8081
```

### 3. Testar o Fluxo
1. Acesse: `http://localhost:8081`
2. Clique em **"Iniciar Fluxo OAuth2"**
3. Você será redirecionado para `http://localhost:8080/login`
4. Faça login com:
   - **Usuário:** `user`
   - **Senha:** `password`
5. Após o login, você será redirecionado para `http://localhost:8081/callback`
6. Você verá o **Authorization Code** e os **tokens** obtidos

## 📊 O que Você Verá na Tela de Resultado

- ✅ **Authorization Code** - Código temporário recebido
- ✅ **Access Token** - Token para acessar recursos
- ✅ **ID Token** - Token com informações do usuário (OpenID Connect)
- ✅ **Refresh Token** - Para renovar tokens sem novo login
- ✅ **Token Type** - Tipo do token (Bearer)
- ✅ **Expires In** - Tempo de expiração do token

## 🔧 Endpoints Importantes

### Authorization Server (8080)
- `GET /oauth2/authorize` - Iniciar autorização
- `POST /oauth2/token` - Obter tokens
- `GET /login` - Página de login
- `GET /.well-known/oauth-authorization-server` - Metadados

### Client Application (8081)
- `GET /` - Página inicial
- `GET /login` - Redireciona para autorização
- `GET /callback` - Recebe código e troca por token

## 📝 Parâmetros da URL de Autorização

Quando você clica em "Iniciar Fluxo OAuth2", é gerada esta URL:

```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=my-client&
  redirect_uri=http://localhost:8081/callback&
  scope=openid profile&
  state=xyz123
```

- **response_type=code** - Solicita authorization code
- **client_id** - Identifica a aplicação cliente
- **redirect_uri** - Para onde voltar após login
- **scope** - Permissões solicitadas
- **state** - Proteção contra CSRF

## ⚠️ Importante para Produção

Esta configuração é **APENAS para estudo**! Em produção:

- ✅ Use `BCryptPasswordEncoder` ao invés de `NoOpPasswordEncoder`
- ✅ Armazene clientes e usuários em banco de dados
- ✅ Use HTTPS em todas as URLs
- ✅ Configure secrets seguros e únicos
- ✅ Implemente rate limiting e outras proteções
- ✅ Use JWT com chaves RSA ao invés de chaves simétricas

## 🐛 Troubleshooting

**Erro de redirecionamento:**
- Verifique se as portas 8080 e 8081 estão livres
- Confirme se as URLs de redirect estão exatamente iguais

**Erro de autenticação:**
- Verifique se o Authorization Server está rodando na porta 8080
- Teste o login direto em `http://localhost:8080/login`

**Token inválido:**
- Verifique se client_id e client_secret estão corretos
- Confirme se os escopos solicitados estão permitidos