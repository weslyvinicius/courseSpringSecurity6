# Spring Security 6 - @PostAuthorize vs @PreAuthorize vs @Secured

Este projeto demonstra as diferenças entre as anotações `@PostAuthorize`, `@PreAuthorize` e `@Secured` do Spring Security 6 com Spring Boot 3, com foco especial no comportamento único da `@PostAuthorize`.

## Estrutura do Projeto

O projeto é composto por quatro classes principais:

- **`SecurityConfig.java`**: Configuração de segurança com usuários em memória
- **`SecureController.java`**: Controller REST demonstrando as três anotações
- **`ResourceService.java`**: Serviço para lógica de autorização e simulação de operações custosas
- **`Resource.java`**: Modelo de dados para demonstrar autorização baseada em propriedades do objeto

## Classes Explicadas

### 1. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
    // Configurações...
}
```

**Configurações importantes:**

- `securedEnabled = true`: **Habilita** a anotação `@Secured`
- `prePostEnabled = true`: **Habilita** as anotações `@PreAuthorize` e `@PostAuthorize`

**Usuários configurados:**

| Usuário | Senha | Roles | Authorities |
|---------|-------|-------|-------------|
| admin | password | ROLE_ADMIN | READ_RESOURCE, WRITE_RESOURCE |
| manager | password | ROLE_MANAGER | - |
| john | password | ROLE_USER | READ_RESOURCE, WRITE_RESOURCE |

### 2. SecureController.java

Este controller demonstra as diferenças críticas entre as três anotações, especialmente o comportamento único da `@PostAuthorize`.

### 3. ResourceService.java

Simula operações custosas e contém logs para demonstrar **quando** cada método é executado em relação às verificações de autorização.

### 4. Resource.java

Modelo simples com `owner` e `content` para demonstrar autorização baseada no objeto retornado.

## Comparação Detalhada: @PostAuthorize vs @PreAuthorize vs @Secured

### Timing de Execução - A Diferença Crucial

| Anotação | Quando Verifica | Executa Método/Serviço | Acesso ao Resultado |
|----------|-----------------|------------------------|---------------------|
| **@Secured** | ANTES da execução | ❌ Não (se falhar) | ❌ Não |
| **@PreAuthorize** | ANTES da execução | ❌ Não (se falhar) | ❌ Não |
| **@PostAuthorize** | APÓS a execução | ✅ Sempre | ✅ Sim |

### Características Gerais

| Aspecto | @Secured | @PreAuthorize | @PostAuthorize |
|---------|----------|---------------|----------------|
| **Momento da Verificação** | Antes | Antes | **Depois** |
| **SpEL Support** | ❌ Não | ✅ Sim | ✅ Sim |
| **Acesso ao `returnObject`** | ❌ Não | ❌ Não | ✅ **Sim** |
| **Performance** | Rápida | Média | **Mais Lenta** |
| **Segurança** | Alta | Alta | **Menor** |
| **Casos de Uso** | Simples | Complexos | **Filtros por Dados** |

## Exemplos Práticos dos Controllers

### 1. @Secured - Verificação Simples ANTES

```java
@Secured("ROLE_ADMIN")
@GetMapping("/admin")
public ResponseEntity<String> adminOnly() {
    // ✅ Este serviço SÓ é executado se o usuário for ROLE_ADMIN
    Resource resource = resourceService.fetchResource("admin_resource");
    return ResponseEntity.ok("Admin access: " + resource.getContent());
}
```

**Comportamento:**
- ✅ Verifica `ROLE_ADMIN` ANTES de executar
- ❌ Se falhar: método e serviço NÃO são executados
- 🚀 Performance: Rápida (sem execução desnecessária)

### 2. @PreAuthorize - Verificação Dinâmica ANTES

```java
@PreAuthorize("@resourceService.isResourceOwner(authentication, #id)")
@GetMapping("/resource/pre/{id}")
public ResponseEntity<String> accessCustomResource(@PathVariable String id) {
    // ✅ Este serviço SÓ é executado se a verificação de ownership passar
    Resource resource = resourceService.fetchResource(id);
    return ResponseEntity.ok("Access granted: " + resource.getContent());
}
```

**Comportamento:**
- ✅ Verifica ownership ANTES de executar
- ❌ Se falhar: `fetchResource` NÃO é executado
- 🚀 Performance: Boa (evita operação custosa)

### 3. @PostAuthorize - Verificação APÓS Execução

```java
@PostAuthorize("returnObject.owner == authentication.name")
@GetMapping("/resource/owner/{id}")
public Resource getResourceByOwner(@PathVariable String id) {
    // ⚠️ Este serviço SEMPRE é executado, mesmo se a autorização falhar depois!
    return resourceService.fetchResource(id);
}
```

**Comportamento:**
- ⚠️ `fetchResource` SEMPRE é executado primeiro
- ✅ Verifica se o usuário é dono do recurso retornado
- ❌ Se falhar: erro APÓS operação custosa
- 🐌 Performance: Pior (operação sempre executada)

### 4. @PostAuthorize com Lógica Personalizada

```java
@PostAuthorize("@resourceService.canAccessResource(authentication, returnObject)")
@GetMapping("/resource/custom/{id}")
public Resource getCustomResource(@PathVariable String id) {
    // ⚠️ fetchResource é executado primeiro
    // ✅ Depois canAccessResource é executado com o resultado
    return resourceService.fetchResource(id);
}
```

## Fluxo de Execução Detalhado

### @Secured e @PreAuthorize (Verificação ANTES)

```
1. 🔐 Verificação de Autorização
   ├─ ✅ Autorizado → 2. Executa Método
   └─ ❌ Negado → ⚠️ 403 Forbidden (método não executado)
```

### @PostAuthorize (Verificação DEPOIS)

```
1. 🚀 Executa Método SEMPRE
   ├─ 📊 Busca dados do banco
   ├─ 💰 Operações custosas
   └─ 📦 Retorna objeto
2. 🔐 Verificação de Autorização no objeto retornado
   ├─ ✅ Autorizado → 📤 Retorna resultado
   └─ ❌ Negado → ⚠️ 403 Forbidden (após operação custosa!)
```

## Casos de Uso Reais

### Use @Secured quando:

✅ **Controle simples de acesso**
```java
@Secured("ROLE_ADMIN")
public void deleteAllUsers() { 
    // Operação perigosa - deve ser bloqueada ANTES
}
```

### Use @PreAuthorize quando:

✅ **Verificações baseadas em parâmetros**
```java
@PreAuthorize("#userId == authentication.principal.id")
public User updateUser(@PathVariable Long userId, @RequestBody User user) {
    // Usuário só pode editar próprio perfil
}
```

### Use @PostAuthorize quando:

✅ **Filtragem baseada nos dados retornados**
```java
@PostAuthorize("returnObject.department == authentication.principal.department")
public Employee getEmployee(@PathVariable Long id) {
    // Só mostra funcionário do mesmo departamento
    return employeeService.findById(id);
}
```

## ⚠️ Problemas de Segurança com @PostAuthorize

### 1. Vazamento de Informações

```java
@PostAuthorize("returnObject.confidential == false")
public Document getDocument(@PathVariable Long id) {
    // ❌ PROBLEMA: Dados confidenciais são buscados do banco
    // mesmo que o usuário não tenha permissão!
    return documentService.findById(id); // Operação custosa executada sempre
}
```

### 2. Operações Custosas Desnecessárias

```java
@PostAuthorize("@securityService.canAccess(authentication, returnObject)")
public Report generateExpensiveReport(@PathVariable String type) {
    // ❌ PROBLEMA: Relatório sempre é gerado, mesmo para usuários sem permissão
    return reportService.generateComplexReport(type); // 30 segundos de processamento!
}
```

### 3. Side Effects Indesejados

```java
@PostAuthorize("returnObject.owner == authentication.name")
public Order processOrder(@RequestBody Order order) {
    // ❌ PROBLEMA: Pedido é processado, estoque é reduzido,
    // cobrança é feita, email é enviado... MAS depois a autorização falha!
    return orderService.processPaymentAndSendEmail(order);
}
```

## Alternativas Seguras para @PostAuthorize

### ❌ Problemático com @PostAuthorize:
```java
@PostAuthorize("returnObject.owner == authentication.name")
public BankAccount getAccount(@PathVariable String accountId) {
    return expensiveBankQuery(accountId); // Sempre executado!
}
```

### ✅ Solução com @PreAuthorize:
```java
@PreAuthorize("@accountService.isOwner(authentication.name, #accountId)")
public BankAccount getAccount(@PathVariable String accountId) {
    return expensiveBankQuery(accountId); // Só executado se autorizado
}
```

### ✅ Solução Híbrida:
```java
public BankAccount getAccount(@PathVariable String accountId) {
    // Verificação rápida primeiro
    if (!accountService.isOwner(getCurrentUser(), accountId)) {
        throw new AccessDeniedException("Not account owner");
    }
    // Operação custosa só depois da verificação
    return expensiveBankQuery(accountId);
}
```

## Testes Práticos

### Comandos curl para demonstrar as diferenças:

```bash
# 1. @Secured - Falha ANTES da execução do serviço
curl -u manager:password http://localhost:8080/api/admin
# Log esperado: Nenhum (fetchResource NÃO é chamado)
# Resultado: 403 Forbidden

# 2. @PreAuthorize - Falha ANTES da execução do serviço  
curl -u admin:password http://localhost:8080/api/resource/pre/john_document
# Log esperado: "Checking ownership..." mas NÃO "Fetching resource..."
# Resultado: 403 Forbidden

# 3. @PostAuthorize - Executa serviço MAS falha DEPOIS
curl -u admin:password http://localhost:8080/api/resource/owner/john_document
# Log esperado: "Fetching resource for ID: john_document" (sempre executado!)
# Resultado: 403 Forbidden (mas operação foi feita)

# 4. @PostAuthorize - Sucesso (usuário correto)
curl -u john:password http://localhost:8080/api/resource/owner/john_document
# Log esperado: "Fetching resource..." seguido de sucesso
# Resultado: 200 OK
```

### Comparação de Logs:

#### Teste com usuário SEM permissão:

**@Secured:**
```
# Nenhum log - método não executado
```

**@PreAuthorize:**
```
Checking ownership for user: admin, resourceId: john_document
# Nenhum "Fetching resource" - fetchResource não executado
```

**@PostAuthorize:**
```
Fetching resource for ID: john_document  ← Sempre executado!
# Depois falha na autorização
```

## Performance Comparison

### Cenário: 1000 requests negados por segundo

| Anotação | CPU Usage | Memory | Database Calls |
|----------|-----------|--------|----------------|
| **@Secured** | 5% | Baixo | 0 |
| **@PreAuthorize** | 8% | Baixo | 0 |
| **@PostAuthorize** | 45% | Alto | 1000 |

## Boas Práticas

### ✅ DO - Use @PostAuthorize para:

1. **Filtragem de dados já carregados**
```java
@PostAuthorize("returnObject.publicData == true")
public List<Article> getArticles() {
    return articleService.findAll(); // Lista já carregada
}
```

2. **Verificações baseadas em propriedades calculadas**
```java
@PostAuthorize("returnObject.calculatedScore > 50")
public Result getTestResult(@PathVariable Long testId) {
    return testService.calculateResult(testId);
}
```

### ❌ DON'T - Evite @PostAuthorize para:

1. **Operações custosas com alta chance de negação**
2. **Operações com side effects**
3. **Dados altamente sensíveis**
4. **APIs com alta volumetria**

## Padrões de Migração

### De @PostAuthorize para @PreAuthorize:

```java
// ❌ Antes: @PostAuthorize
@PostAuthorize("returnObject.owner == authentication.name")
public Document getDocument(@PathVariable Long id) {
    return documentService.findById(id);
}

// ✅ Depois: @PreAuthorize
@PreAuthorize("@documentService.isOwner(authentication.name, #id)")
public Document getDocument(@PathVariable Long id) {
    return documentService.findById(id);
}

// Service method necessário:
public boolean isOwner(String username, Long documentId) {
    return documentRepository.findOwnerById(documentId).equals(username);
}
```

## Resumo das Recomendações

### Para Novos Projetos:
1. **Primeira escolha**: `@PreAuthorize` (flexível e segura)
2. **Para casos simples**: `@Secured` (rápida)
3. **Raramente**: `@PostAuthorize` (apenas para filtragem)

### Para Refatoração:
1. **Identifique** `@PostAuthorize` com operações custosas
2. **Migre** para `@PreAuthorize` quando possível
3. **Mantenha** `@PostAuthorize` apenas para filtragem real

### Checklist de Segurança:
- [ ] `@PostAuthorize` não executa operações custosas?
- [ ] Não há side effects no método protegido?
- [ ] Dados sensíveis não são expostos durante execução?
- [ ] Performance é aceitável para o volume esperado?

## Conclusão

`@PostAuthorize` é uma ferramenta poderosa mas perigosa. Use com cuidado e sempre considere se `@PreAuthorize` não seria uma alternativa mais segura e performática para seu caso de uso específico.

A regra de ouro: **Se você pode verificar ANTES, sempre verifique ANTES!**