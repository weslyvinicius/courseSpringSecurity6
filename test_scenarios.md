# Cenários de Teste - OAuth2 Authorization Server

## 🔧 URLs de Teste

### 1. **Mobile App + John (Employee)**
```
URL de Autorização:
http://localhost:8080/oauth2/authorize?
  client_id=mobile-app&
  response_type=code&
  scope=openid profile employee.read report.read&
  redirect_uri=com.academy.app://callback&
  state=123

Credenciais: john / j123456
```

**Token Esperado:**
```json
{
  "sub": "john",
  "scope": ["openid", "profile", "employee.read", "report.read"],
  "authorities": ["READ_EMPLOYEE", "ROLE_EMPLOYEE"],
  "username": "john"
}
```
*Note: `report.read` foi solicitado mas John não tem `READ_REPORT`*

---

### 2. **Dashboard + Mary (Manager)**
```
URL de Autorização:
http://localhost:8080/oauth2/authorize?
  client_id=dashboard-app&
  response_type=code&
  scope=openid profile employee.read employee.write report.read report.write&
  redirect_uri=http://localhost:8082/callback&
  state=456

Credenciais: mary / m123456
```

**Token Esperado:**
```json
{
  "sub": "mary",
  "scope": ["openid", "profile", "employee.read", "employee.write", "report.read", "report.write"],
  "authorities": ["READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT", "ROLE_MANAGER"],
  "username": "mary"
}
```
*Note: `report.write` solicitado mas Mary não tem `CREATE_REPORT`/`UPDATE_REPORT`*

---

### 3. **Web App + Susan (Admin)**
```
URL de Autorização:
http://localhost:8080/oauth2/authorize?
  client_id=web-app&
  response_type=code&
  scope=openid profile employee.read employee.write employee.delete report.read report.write report.delete admin&
  redirect_uri=http://localhost:8081/callback&
  state=789

Credenciais: susan / s123456
```

**Token Esperado:**
```json
{
  "sub": "susan",
  "scope": ["openid", "profile", "employee.read", "employee.write", "employee.delete", "report.read", "report.write", "report.delete", "admin"],
  "authorities": ["ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"],
  "username": "susan"
}
```

---

### 4. **Reports App + Susan (Admin)**
```
URL de Autorização:
http://localhost:8080/oauth2/authorize?
  client_id=reports-app&
  response_type=code&
  scope=openid profile report.read report.write&
  redirect_uri=http://localhost:8083/callback&
  state=101

Credenciais: susan / s123456
```

**Token Esperado:**
```json
{
  "sub": "susan",
  "scope": ["openid", "profile", "report.read", "report.write"],
  "authorities": ["READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "ROLE_ADMIN"],
  "username": "susan"
}
```
*Note: Susan é admin mas só tem acesso a relatórios neste cliente*

---

### 5. **Mobile App + Susan (Admin)**
```
URL de Autorização:
http://localhost:8080/oauth2/authorize?
  client_id=mobile-app&
  response_type=code&
  scope=openid profile employee.read report.read&
  redirect_uri=com.academy.app://callback&
  state=202

Credenciais: susan / s123456
```

**Token Esperado:**
```json
{
  "sub": "susan",
  "scope": ["openid", "profile", "employee.read", "report.read"],
  "authorities": ["READ_EMPLOYEE", "READ_REPORT", "ROLE_ADMIN"],
  "username": "susan"
}
```
*Note: Admin usando app móvel = apenas leitura (limitação do cliente)*

---

## 🧪 Como Testar

### 1. **Fazer Requisição de Autorização**
- Cole a URL no navegador
- Faça login com as credenciais do usuário
- Autorize o acesso na tela de consentimento
- Copie o `code` da URL de callback

### 2. **Trocar Code por Token**
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic <CLIENT_CREDENTIALS_BASE64>" \
  -d "grant_type=authorization_code&code=<CODE>&redirect_uri=<REDIRECT_URI>"
```

### 3. **Decodificar o JWT**
- Vá para https://jwt.io
- Cole o `access_token` recebido
- Verifique o payload para confirmar as `authorities`

---

## 📊 Matriz de Permissões

| Cliente / Usuário | John (Employee) | Mary (Manager) | Susan (Admin) |
|-------------------|-----------------|----------------|---------------|
| **Mobile App** | ✅ employee.read | ✅ employee.read<br>✅ report.read | ✅ employee.read<br>✅ report.read |
| **Dashboard** | ✅ employee.read | ✅ employee.read<br>✅ employee.write<br>✅ report.read | ✅ Tudo exceto delete |
| **Web App** | ✅ employee.read | ✅ employee.read<br>✅ employee.write<br>✅ report.read | ✅ Acesso completo |
| **Reports App** | ❌ Sem acesso | ✅ report.read | ✅ report.read<br>✅ report.write |

---

## 🔍 Pontos Importantes

1. **John** só consegue ler employees, mesmo que o cliente solicite mais escopos
2. **Mary** tem acesso de manager, mas limitado pelos escopos do cliente
3. **Susan** tem acesso completo, mas ainda limitado pelos escopos que cada cliente pode solicitar
4. **Princípio do Menor Privilégio**: O token final sempre contém a INTERSEÇÃO entre:
   - Escopos que o cliente PODE solicitar
   - Authorities que o usuário POSSUI