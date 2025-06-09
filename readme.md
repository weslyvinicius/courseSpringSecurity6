# Spring Security 6 - @PreFilter vs @PreAuthorize vs @Secured

Este projeto demonstra as diferenças entre as anotações `@PreFilter`, `@PreAuthorize` e `@Secured` do Spring Security 6 com Spring Boot 3, com foco especial no comportamento de filtragem da `@PreFilter`.

## Estrutura do Projeto

O projeto é composto por quatro classes principais:

- **`SecurityConfig.java`**: Configuração de segurança com usuários em memória
- **`SecureController.java`**: Controller REST demonstrando as três anotações
- **`ResourceService.java`**: Serviço para lógica de autorização e manipulação de recursos
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
- `prePostEnabled = true`: **Habilita** as anotações `@PreAuthorize`, `@PostAuthorize`, `@PreFilter` e `@PostFilter`

**Usuários configurados:**

| Usuário | Senha | Roles | Authorities |
|---------|-------|-------|-------------|
| admin | password | ROLE_ADMIN | READ_RESOURCE |
| john | password | ROLE_USER | READ_RESOURCE |
| other | password | ROLE_USER | - |

### 2. SecureController.java

Este controller demonstra as diferenças críticas entre as três anotações, especialmente o comportamento único da `@PreFilter`.

### 3. ResourceService.java

Contém a lógica de autorização personalizada e logs para demonstrar **quando** e **como** cada método é executado.

### 4. Resource.java

Modelo simples com `owner` e `content` para demonstrar autorização baseada no objeto.

## Comparação Detalhada: @PreFilter vs @PreAuthorize vs @Secured

### Propósito Principal - A Diferença Fundamental

| Anotação | Propósito | Tipo de Verificação | Resultado |
|----------|-----------|---------------------|-----------|
| **@Secured** | Controle de Acesso | Role/Authority | ✅ Executa ou ❌ Bloqueia |
| **@PreAuthorize** | Controle de Acesso | Condição Booleana | ✅ Executa ou ❌ Bloqueia |
| **@PreFilter** | **Filtragem de Dados** | **Filtro por Item** | **🔍 Filtra Coleção** |

### Características Técnicas

| Aspecto | @Secured | @PreAuthorize | @PreFilter |
|---------|----------|---------------|------------|
| **Momento da Verificação** | Antes | Antes | **Antes (Filtragem)** |
| **SpEL Support** | ❌ Não | ✅ Sim | ✅ Sim |
| **Trabalha com Coleções** | ❌ Não | ❌ Não | ✅ **Exclusivamente** |
| **Tipo de Controle** | Tudo ou Nada | Tudo ou Nada | **Item por Item** |
| **Performance** | Rápida | Média | **Variável** |
| **Uso Típico** | Roles Simples | Lógica Complexa | **Filtros de Lista** |

## Como @PreFilter Funciona

### Conceito Central

`@PreFilter` **não bloqueia** a execução do método. Em vez disso, ela **filtra** a coleção de entrada, removendo itens que não atendem à condição especificada.

### Fluxo de Execução da @PreFilter

```
1. 📥 Recebe Lista Original: [item1, item2, item3, item4, item5]
2. 🔍 Aplica Filtro em Cada Item:
   ├─ item1 → ✅ Passa no filtro
   ├─ item2 → ❌ Não passa
   ├─ item3 → ✅ Passa no filtro  
   ├─ item4 → ❌ Não passa
   └─ item5 → ✅ Passa no filtro
3. 📤 Lista Filtrada: [item1, item3, item5]
4. 🚀 Executa Método com Lista Filtrada
```

## Exemplos Práticos dos Controllers

### 1. @Secured - Controle de Acesso Simples

```java
@Secured("ROLE_ADMIN")
@GetMapping("/admin")
public ResponseEntity<String> adminOnly() {
    // ✅ SÓ executa se usuário for ROLE_ADMIN
    // ❌ Se não for: 403 Forbidden (método não executado)
    List<Resource> resources = resourceService.fetchResources(List.of("admin_resource"));
    return ResponseEntity.ok("Admin access: " + resources.get(0).getContent());
}
```

### 2. @PreAuthorize - Controle de Acesso Condicional

```java
@PreAuthorize("@resourceService.isResourceOwner(authentication, #resourceId)")
@GetMapping("/resource/pre/{resourceId}")
public ResponseEntity<String> accessCustomResource(@PathVariable String resourceId) {
    // ✅ SÓ executa se usuário for dono do recurso
    // ❌ Se não for: 403 Forbidden (método não executado)
    List<Resource> resources = resourceService.fetchResources(List.of(resourceId));
    return ResponseEntity.ok("Access granted: " + resources.get(0).getContent());
}
```

### 3. @PreFilter - Filtragem de Lista Simples

```java
@PreFilter("filterObject.split('_')[0] == authentication.name")
@PostMapping("/resources/filter")
public ResponseEntity<List<Resource>> filterResources(@RequestBody List<String> resourceIds) {
    // 🔍 Lista resourceIds é FILTRADA antes da execução
    // ✅ Método SEMPRE executa (mesmo com lista vazia)
    // Só resourceIds cujo prefixo corresponde ao username são mantidos
    List<Resource> resources = resourceService.fetchResources(resourceIds);
    return ResponseEntity.ok(resources);
}
```

**Exemplo de funcionamento:**
- **Input**: `["john_doc1", "admin_doc2", "john_doc3", "other_doc4"]`
- **Usuário logado**: `john`
- **Após @PreFilter**: `["john_doc1", "john_doc3"]`
- **Resultado**: Só documentos do John são processados

### 4. @PreFilter - Filtragem com Lógica Personalizada

```java
@PreFilter(value = "@resourceService.canAccessResource(authentication, filterObject)", 
           filterTarget = "resources")
@PostMapping("/resources/custom-filter")
public ResponseEntity<List<Resource>> customFilterResources(@RequestBody List<Resource> resources) {
    // 🔍 Lista resources é filtrada usando lógica personalizada
    // ✅ Método sempre executa com lista filtrada
    // Não chama resourceService.fetchResources pois os recursos já estão na lista
    return ResponseEntity.ok(resources);
}
```

## Diferenças Práticas entre @PreFilter e @PreAuthorize

### Cenário: Listagem de Documentos

#### ❌ Problema com @PreAuthorize
```java
// Não funciona bem para listas!
@PreAuthorize("@documentService.canAccessAny(authentication, #documentIds)")
@PostMapping("/documents")
public List<Document> getDocuments(@RequestBody List<String> documentIds) {
    // Problemas:
    // 1. Como verificar permissão para CADA documento?
    // 2. Se 1 documento for negado, TODA a operação falha
    // 3. Tudo ou nada - não há filtragem granular
    return documentService.findAll(documentIds);
}
```

#### ✅ Solução com @PreFilter
```java
@PreFilter("@documentService.canAccess(authentication, filterObject)")
@PostMapping("/documents") 
public List<Document> getDocuments(@RequestBody List<String> documentIds) {
    // Vantagens:
    // 1. Cada documento é verificado individualmente
    // 2. Documentos sem permissão são removidos da lista
    // 3. Operação prossegue com documentos autorizados
    return documentService.findAll(documentIds); // Lista já filtrada
}
```

## Casos de Uso Reais

### Use @Secured quando:

✅ **Controle simples de papel/autoridade**
```java
@Secured("ROLE_ADMIN")
public void deleteAllUsers() { 
    // Só admins podem executar
}
```

### Use @PreAuthorize quando:

✅ **Verificação condicional única**
```java
@PreAuthorize("#userId == authentication.principal.id")
public User updateProfile(@PathVariable Long userId, @RequestBody User user) {
    // Só pode editar próprio perfil
}
```

✅ **Verificação baseada em parâmetros**
```java
@PreAuthorize("@orderService.isOwner(authentication, #orderId)")
public Order getOrder(@PathVariable Long orderId) {
    // Só dono do pedido pode visualizar
}
```

### Use @PreFilter quando:

✅ **Filtragem de listas baseada em permissões**
```java
@PreFilter("@securityService.canViewProject(authentication, filterObject)")
public List<Project> getProjects(@RequestBody List<String> projectIds) {
    // Filtra projetos que o usuário pode ver
}
```

✅ **Processamento em lote com controle granular**
```java
@PreFilter("filterObject.department == authentication.principal.department")
public BatchResult processEmployees(@RequestBody List<Employee> employees) {
    // Processa só funcionários do mesmo departamento
}
```

✅ **APIs de busca com filtros de segurança**
```java
@PreFilter("@documentService.hasReadPermission(authentication, filterObject.id)")
public List<Document> searchDocuments(@RequestBody List<SearchCriteria> criteria) {
    // Busca só documentos com permissão de leitura
}
```

## Atributos Especiais da @PreFilter

### 1. filterTarget (Para múltiplos parâmetros)

```java
@PreFilter(value = "filterObject.owner == authentication.name", 
           filterTarget = "documents")
public Result processMultipleData(@RequestBody List<Document> documents, 
                                 @RequestBody List<String> categories) {
    // Especifica que o filtro se aplica ao parâmetro 'documents'
    // 'categories' não é filtrado
}
```

### 2. filterObject (Referência ao item atual)

```java
@PreFilter("filterObject.startsWith(authentication.name)")
public List<String> filterPrefixes(@RequestBody List<String> items) {
    // filterObject se refere a cada string individual da lista
}
```

### 3. Filtros Complexos com SpEL

```java
@PreFilter("filterObject.priority > 5 and @securityService.hasAccess(authentication, filterObject)")
public List<Task> processHighPriorityTasks(@RequestBody List<Task> tasks) {
    // Combina múltiplas condições
}
```

## Comparação de Performance

### Cenário: 1000 itens, 50% autorizados

| Abordagem | Tempo de Verificação | Itens Processados | Eficiência |
|-----------|---------------------|-------------------|------------|
| **Sem Filtro** | 0ms | 1000 | ❌ Dados não autorizados |
| **@PreAuthorize** | 5ms | 0 ou 1000 | ❌ Tudo ou nada |
| **@PreFilter** | 15ms | 500 | ✅ Só dados autorizados |
| **Filtro Manual** | 25ms | 500 | ⚠️ Código duplicado |

## Limitações da @PreFilter

### ❌ Não Funciona Com:

1. **Parâmetros únicos (não-coleção)**
```java
// ERRO: @PreFilter só funciona com coleções
@PreFilter("filterObject == authentication.name")
public User getUser(String username) { } // Não é uma coleção!
```

2. **Verificações de método inteiro**
```java
// Use @PreAuthorize em vez de @PreFilter
@PreFilter("hasRole('ADMIN')") // Erro conceitual!
public List<User> getAllUsers() { }
```

3. **Filtragem baseada em contexto externo**
```java
// Não tem acesso a outras variáveis do método
@PreFilter("filterObject.date > #startDate") // #startDate não disponível
public List<Event> getEvents(List<String> eventIds, LocalDate startDate) { }
```

## Testes Práticos

### Setup de Teste com curl:

```bash
# 1. @Secured - Sucesso para admin
curl -X GET -u admin:password http://localhost:8080/api/admin
# Resultado: 200 OK

# 2. @Secured - Falha para john  
curl -X GET -u john:password http://localhost:8080/api/admin
# Resultado: 403 Forbidden

# 3. @PreAuthorize - Sucesso (john acessa próprio recurso)
curl -X GET -u john:password http://localhost:8080/api/resource/pre/john_document
# Resultado: 200 OK

# 4. @PreAuthorize - Falha (john tenta acessar recurso do admin)
curl -X GET -u john:password http://localhost:8080/api/resource/pre/admin_document  
# Resultado: 403 Forbidden

# 5. @PreFilter - Filtragem em ação
curl -X POST -u john:password \
  -H "Content-Type: application/json" \
  -d '["john_doc1", "admin_doc2", "john_doc3", "other_doc4"]' \
  http://localhost:8080/api/resources/filter
# Resultado: 200 OK com apenas ["john_doc1", "john_doc3"]
```

### Comparação de Logs:

#### @PreAuthorize (Acesso Negado):
```
Checking ownership for user: john, resourceId: admin_document
# Método não executado - 403 Forbidden
```

#### @PreFilter (Filtragem):
```
# Lista original: ["john_doc1", "admin_doc2", "john_doc3", "other_doc4"]
# Aplicando filtro para cada item...
Fetching resources for IDs: [john_doc1, john_doc3]
# Método executado com lista filtrada
```

## Combinações Poderosas

### @PreFilter + @PreAuthorize

```java
@PreAuthorize("hasAuthority('READ_RESOURCE')")
@PreFilter("@resourceService.isResourceOwner(authentication, filterObject)")
@PostMapping("/secure-filter")
public List<Resource> secureFilteredAccess(@RequestBody List<String> resourceIds) {
    // 1. @PreAuthorize: Verifica se usuário tem READ_RESOURCE
    // 2. @PreFilter: Filtra IDs que pertencem ao usuário
    // 3. Método executa só se ambas passarem
    return resourceService.fetchResources(resourceIds);
}
```

### Validação de Entrada + Filtragem

```java
@PreFilter("!filterObject.isEmpty() and filterObject.length() > 3")
@PostMapping("/validated-filter")
public List<Result> processValidItems(@RequestBody List<String> items) {
    // Remove itens vazios ou muito curtos antes do processamento
    return processItems(items);
}
```

## Padrões Recomendados

### ✅ Padrão: Filtragem + Verificação

```java
@PreAuthorize("hasAuthority('PROCESS_BATCH')")
@PreFilter("@securityService.canProcess(authentication, filterObject)")
public BatchResult processBatch(@RequestBody List<BatchItem> items) {
    // Combina controle de acesso geral + filtragem granular
    return batchService.process(items);
}
```

### ✅ Padrão: Filtragem Condicional

```java
public List<Document> getDocuments(@RequestBody List<String> docIds, 
                                  @RequestParam boolean onlyOwned) {
    if (onlyOwned) {
        return getOwnedDocuments(docIds);
    }
    return getAllDocuments(docIds);
}

@PreFilter("@docService.isOwner(authentication, filterObject)")
private List<Document> getOwnedDocuments(List<String> docIds) {
    return documentService.findAll(docIds);
}
```

## Troubleshooting Comum

### Problema 1: @PreFilter não funciona
```java
// ❌ Erro comum
@PreFilter("filterObject == authentication.name")
public String getUsername(String username) { } // NÃO é coleção!

// ✅ Correção
@PreAuthorize("#username == authentication.name")
public String getUsername(String username) { }
```

### Problema 2: filterTarget incorreto
```java
// ❌ Erro: múltiplos parâmetros sem filterTarget
@PreFilter("filterObject.owner == authentication.name")
public Result process(List<Document> docs, List<String> categories) { }

// ✅ Correção
@PreFilter(value = "filterObject.owner == authentication.name", 
           filterTarget = "docs")
public Result process(List<Document> docs, List<String> categories) { }
```

### Problema 3: Performance com listas grandes
```java
// ❌ Problemático para listas muito grandes
@PreFilter("@expensiveService.checkPermission(authentication, filterObject)")
public List<Item> processLargeList(@RequestBody List<Item> items) { }

// ✅ Otimização: pré-filtrar no repository
@PreAuthorize("hasAuthority('BULK_PROCESS')")
public List<Item> processLargeList(@RequestParam String userId) {
    // Filtra no banco de dados em vez de na aplicação
    return itemRepository.findByOwner(userId);
}
```

## Resumo das Recomendações

### Para Novos Projetos:
1. **@PreAuthorize** para controle de acesso geral
2. **@PreFilter** para filtragem de listas com segurança
3. **@Secured** para casos muito simples de role

### Para APIs REST:
1. **GET individual**: `@PreAuthorize`
2. **GET lista**: `@PreFilter` ou filtragem no repository
3. **POST/PUT**: `@PreAuthorize` + validação
4. **DELETE**: `@PreAuthorize` (operação sensível)

### Checklist de Segurança:
- [ ] `@PreFilter` é necessária ou posso filtrar no repository?
- [ ] A lista pode ser muito grande para filtragem em memória?
- [ ] Combinei `@PreFilter` com `@PreAuthorize` quando necessário?
- [ ] Testei com listas vazias e casos extremos?

## Conclusão

`@PreFilter` é uma ferramenta especializada para filtragem granular de coleções baseada em permissões. Use quando precisar processar listas onde diferentes itens têm diferentes níveis de acesso.

**Regra de Ouro**: 
- **Controle de Acesso** → `@PreAuthorize`
- **Filtragem de Lista** → `@PreFilter`
- **Papéis Simples** → `@Secured`

A escolha correta depende do seu caso de uso específico: você quer **bloquear completamente** ou **filtrar seletivamente**?