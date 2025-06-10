# Spring Security 6 - @PostFilter vs @PreFilter vs @PreAuthorize vs @Secured

Este projeto demonstra as diferenças entre as anotações `@PostFilter`, `@PreFilter`, `@PreAuthorize` e `@Secured` do Spring Security 6 com Spring Boot 3, com foco especial no comportamento único da `@PostFilter`.

## Estrutura do Projeto

O projeto é composto por quatro classes principais:

- **`SecurityConfig.java`**: Configuração de segurança com usuários em memória
- **`SecureController.java`**: Controller REST demonstrando as quatro anotações
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

Este controller demonstra as diferenças críticas entre as quatro anotações, especialmente o comportamento único da `@PostFilter` em comparação com `@PreFilter`.

### 3. ResourceService.java

Contém a lógica de autorização personalizada e logs para demonstrar **quando** e **como** cada método é executado.

### 4. Resource.java

Modelo simples com `owner` e `content` para demonstrar autorização baseada no objeto.

## Comparação Detalhada: @PostFilter vs @PreFilter vs @PreAuthorize vs @Secured

### Propósito Principal - A Diferença Fundamental

| Anotação | Propósito | Momento da Verificação | Tipo de Verificação | Resultado |
|----------|-----------|------------------------|---------------------|-----------|
| **@Secured** | Controle de Acesso | **Antes** | Role/Authority | ✅ Executa ou ❌ Bloqueia |
| **@PreAuthorize** | Controle de Acesso | **Antes** | Condição Booleana | ✅ Executa ou ❌ Bloqueia |
| **@PreFilter** | **Filtragem de Entrada** | **Antes** | **Filtro por Item** | **🔍 Filtra Entrada** |
| **@PostFilter** | **Filtragem de Saída** | **Depois** | **Filtro por Item** | **🔍 Filtra Resultado** |

### Características Técnicas

| Aspecto | @Secured | @PreAuthorize | @PreFilter | @PostFilter |
|---------|----------|---------------|------------|-------------|
| **Momento da Verificação** | Antes | Antes | **Antes (Entrada)** | **Depois (Saída)** |
| **SpEL Support** | ❌ Não | ✅ Sim | ✅ Sim | ✅ Sim |
| **Trabalha com Coleções** | ❌ Não | ❌ Não | ✅ Sim | ✅ **Sim** |
| **Tipo de Controle** | Tudo ou Nada | Tudo ou Nada | Item por Item | **Item por Item** |
| **Performance** | Rápida | Média | Variável | **Pode ser Cara** |
| **Impacto no Processamento** | Bloqueia | Bloqueia | **Reduz Entrada** | **Processa Tudo** |
| **Uso Típico** | Roles Simples | Lógica Complexa | Filtros de Lista | **Filtros de Resultado** |

## A Diferença Crucial: @PreFilter vs @PostFilter

### Conceito Central

- **@PreFilter**: Filtra a **entrada** antes do método executar → **Menor processamento**
- **@PostFilter**: Filtra a **saída** depois do método executar → **Processamento completo**

### Fluxo de Execução Comparado

#### @PreFilter (Filtragem de Entrada)
```
1. 📥 Recebe Lista: [item1, item2, item3, item4, item5]
2. 🔍 Aplica Filtro ANTES:
   ├─ item1 → ✅ Passa (mantém)
   ├─ item2 → ❌ Remove
   ├─ item3 → ✅ Passa (mantém)
   ├─ item4 → ❌ Remove
   └─ item5 → ✅ Passa (mantém)
3. 📤 Lista Filtrada: [item1, item3, item5]
4. 🚀 Executa Método: Processa APENAS 3 itens
5. ✅ Retorna: Resultado dos 3 itens processados
```

#### @PostFilter (Filtragem de Saída)
```
1. 📥 Recebe Lista: [item1, item2, item3, item4, item5]
2. 🚀 Executa Método: Processa TODOS os 5 itens
3. 📤 Resultado Completo: [result1, result2, result3, result4, result5]
4. 🔍 Aplica Filtro DEPOIS:
   ├─ result1 → ✅ Passa (mantém)
   ├─ result2 → ❌ Remove
   ├─ result3 → ✅ Passa (mantém)
   ├─ result4 → ❌ Remove
   └─ result5 → ✅ Passa (mantém)
5. ✅ Retorna: [result1, result3, result5]
```

## Exemplos Práticos dos Controllers

### 1. @PostFilter - Filtragem de Resultado

```java
@PostFilter("filterObject.owner == authentication.name")
@GetMapping("/resources/post-filter")
public List<Resource> getPostFilteredResources() {
    // 🚀 SEMPRE executa fetchResources com TODOS os IDs
    // 🔍 DEPOIS filtra o resultado baseado no owner
    // ⚠️ Processamento completo mesmo para itens que serão removidos
    List<String> resourceIds = List.of("john_resource1", "other_resource2", "john_resource3");
    System.out.println("Fetching ALL resources: " + resourceIds);
    return resourceService.fetchResources(resourceIds); // Processa todos, filtra depois
}
```

**Comportamento:**
- **Input**: `["john_resource1", "other_resource2", "john_resource3"]`
- **Processamento**: Busca TODOS os 3 recursos no serviço
- **Usuário logado**: `john`
- **Após @PostFilter**: Remove `other_resource2`, mantém apenas os do John
- **Output**: `[{owner: "john", content: "Content for john_resource1"}, {owner: "john", content: "Content for john_resource3"}]`

### 2. @PreFilter - Filtragem de Entrada (Comparação)

```java
@PreFilter("filterObject.split('_')[0] == authentication.name")
@PostMapping("/resources/filter")
public ResponseEntity<List<Resource>> filterResources(@RequestBody List<String> resourceIds) {
    // 🔍 PRIMEIRO filtra resourceIds baseado no prefixo
    // 🚀 DEPOIS executa fetchResources apenas com IDs filtrados
    // ✅ Processamento otimizado - só processa itens autorizados
    System.out.println("Filtered resourceIds: " + resourceIds);
    List<Resource> resources = resourceService.fetchResources(resourceIds);
    return ResponseEntity.ok(resources);
}
```

**Comportamento:**
- **Input**: `["john_doc1", "other_doc2", "john_doc3"]`
- **Usuário logado**: `john`
- **Após @PreFilter**: `["john_doc1", "john_doc3"]`
- **Processamento**: Busca APENAS os 2 recursos filtrados
- **Output**: Recursos apenas do John

## Quando Usar Cada Anotação

### Use @PostFilter quando:

✅ **Não pode controlar a entrada do método**
```java
@PostFilter("filterObject.department == authentication.principal.department")
@GetMapping("/employees")
public List<Employee> getAllEmployees() {
    // Busca TODOS os funcionários, filtra por departamento depois
    // Útil quando o método sempre deve processar tudo
    return employeeService.findAll();
}
```

✅ **Resultado depende de processamento complexo**
```java
@PostFilter("@reportService.canViewReport(authentication, filterObject)")
@GetMapping("/reports")
public List<Report> generateReports() {
    // Gera TODOS os relatórios primeiro (processamento pesado)
    // Filtra baseado em permissões DEPOIS da geração
    return reportService.generateAll();
}
```

✅ **Filtragem baseada em propriedades calculadas**
```java
@PostFilter("filterObject.score >= authentication.principal.minScore")
@GetMapping("/results")
public List<TestResult> getResults() {
    // Calcula scores de TODOS os resultados
    // Filtra baseado no score calculado
    return testService.calculateScores();
}
```

### Use @PreFilter quando:

✅ **Pode controlar a entrada e quer otimizar performance**
```java
@PreFilter("@securityService.canAccess(authentication, filterObject)")
@PostMapping("/documents")
public List<Document> getDocuments(@RequestBody List<String> docIds) {
    // Filtra IDs ANTES de buscar documentos
    // Performance otimizada - só busca documentos autorizados
    return documentService.findByIds(docIds);
}
```

## Implicações de Performance

### Cenário: 1000 recursos, usuário tem acesso a 100

| Abordagem | Processamento | Itens Processados | Eficiência | Uso de CPU |
|-----------|---------------|-------------------|------------|------------|
| **@PreFilter** | Mínimo | 100 | ✅ **Muito Alta** | ⚡ Baixo |
| **@PostFilter** | Máximo | 1000 | ❌ **Baixa** | 🔥 Alto |
| **Sem Filtro** | Máximo | 1000 | ❌ Dados não filtrados | 🔥 Alto |

### Exemplo Prático de Performance

```java
// ❌ @PostFilter - Performance Ruim
@PostFilter("filterObject.owner == authentication.name")
@GetMapping("/heavy-computation")
public List<ComplexResult> getComplexResults() {
    // 1. Executa cálculo pesado para TODOS os 10.000 itens (30 segundos)
    // 2. Filtra resultado, mantém apenas 50 itens do usuário
    // 3. Desperdício: 99.5% do processamento foi inútil
    return heavyComputationService.processAll(); // Muito lento!
}

// ✅ Alternativa Otimizada
@PreAuthorize("hasAuthority('READ_RESULTS')")
@GetMapping("/heavy-computation")
public List<ComplexResult> getComplexResults() {
    // 1. Busca apenas itens do usuário autenticado (0.1 segundo)
    // 2. Executa cálculo pesado apenas para 50 itens (0.15 segundos)
    // 3. Performance: 200x mais rápido!
    String userId = getCurrentUserId();
    return heavyComputationService.processForUser(userId); // Muito rápido!
}
```

## Casos de Uso Reais para @PostFilter

### 1. Sistema de Relatórios Dinâmicos

```java
@PostFilter("@reportSecurity.canViewReport(authentication, filterObject)")
@GetMapping("/reports/dashboard")
public List<DashboardReport> getDashboardReports() {
    // Gera todos os relatórios do dashboard
    // Filtra baseado em permissões complexas calculadas dinamicamente
    List<DashboardReport> allReports = reportGenerator.generateDashboard();
    
    // @PostFilter remove relatórios que o usuário não pode ver
    // Útil porque permissões dependem do conteúdo do relatório gerado
    return allReports;
}
```

### 2. Sistema de Notificações

```java
@PostFilter("!filterObject.isRead() or filterObject.recipientId == authentication.principal.id")
@GetMapping("/notifications")
public List<Notification> getNotifications() {
    // Busca todas as notificações relevantes
    // Filtra para mostrar apenas:
    // - Notificações não lidas (públicas)
    // - Notificações do próprio usuário (lidas ou não)
    return notificationService.getRecentNotifications();
}
```

### 3. Sistema de Cache com Filtragem

```java
@PostFilter("@permissionService.hasReadAccess(authentication, filterObject)")
@Cacheable("documents")
@GetMapping("/documents/cached")
public List<Document> getCachedDocuments() {
    // Método busca todos os documentos do cache
    // Cache não pode ser filtrado por usuário (compartilhado)
    // @PostFilter aplica segurança individual por usuário
    return documentCache.getAllDocuments();
}
```

## Limitações e Armadilhas da @PostFilter

### ❌ Limitação 1: Performance com Grandes Volumes

```java
// PERIGOSO: @PostFilter com operações custosas
@PostFilter("@expensiveService.checkComplexPermission(authentication, filterObject)")
@GetMapping("/expensive-data")
public List<ExpensiveData> getExpensiveData() {
    // Problemas:
    // 1. Processa TODOS os dados (custoso)
    // 2. Chama serviço caro para CADA item no resultado
    // 3. Pode causar timeout ou consumo excessivo de recursos
    return expensiveService.processAllData(); // Muito caro!
}

// ✅ Solução: Filtrar na fonte
@PreAuthorize("hasAuthority('VIEW_DATA')")
@GetMapping("/expensive-data")
public List<ExpensiveData> getExpensiveData() {
    String userId = getCurrentUserId();
    return expensiveService.processDataForUser(userId); // Eficiente!
}
```

### ❌ Limitação 2: Efeitos Colaterais Indesejados

```java
// PROBLEMA: Métodos com efeitos colaterais
@PostFilter("filterObject.owner == authentication.name")
@PostMapping("/process-orders")
public List<OrderResult> processOrders(@RequestBody List<Order> orders) {
    // PERIGO: Processa TODOS os pedidos (inclusive não autorizados)
    // Efeitos colaterais: emails enviados, estoque reduzido, etc.
    // @PostFilter só filtra o resultado, não desfaz o processamento!
    return orderProcessor.processAll(orders); // Processamento indevido!
}

// ✅ Solução: @PreFilter
@PreFilter("filterObject.customerId == authentication.principal.id")
@PostMapping("/process-orders")
public List<OrderResult> processOrders(@RequestBody List<Order> orders) {
    // Filtra pedidos ANTES do processamento
    // Só processa pedidos autorizados
    return orderProcessor.processAll(orders); // Seguro!
}
```

### ❌ Limitação 3: Logs e Auditoria Confusos

```java
@PostFilter("filterObject.visible")
@GetMapping("/audit-logs")
public List<AuditLog> getAuditLogs() {
    List<AuditLog> allLogs = auditService.getAllLogs();
    
    // Problema: Logs mostram que TODOS os registros foram acessados
    // Mas usuário só vê os filtrados
    // Auditoria fica inconsistente
    return allLogs;
}
```

## Combinações Avançadas

### @PostFilter + @PreAuthorize

```java
@PreAuthorize("hasAuthority('VIEW_REPORTS')")
@PostFilter("@reportService.canAccessReport(authentication, filterObject)")
@GetMapping("/secure-reports")
public List<Report> getSecureReports() {
    // 1. @PreAuthorize: Verifica permissão geral para ver relatórios
    // 2. Método executa: Gera todos os relatórios
    // 3. @PostFilter: Remove relatórios específicos sem permissão
    return reportService.generateAllReports();
}
```

### @PreFilter + @PostFilter (Dupla Filtragem)

```java
@PreFilter("filterObject.priority >= 5")
@PostFilter("filterObject.owner == authentication.name")
@PostMapping("/process-tasks")
public List<TaskResult> processTasks(@RequestBody List<Task> tasks) {
    // 1. @PreFilter: Remove tasks com prioridade baixa
    // 2. Método executa: Processa apenas tasks de alta prioridade  
    // 3. @PostFilter: Remove resultados que não pertencem ao usuário
    // Útil quando: entrada e saída têm critérios de filtragem diferentes
    return taskProcessor.process(tasks);
}
```

## Alternativas Mais Eficientes

### Padrão Repository com Filtragem

```java
// ❌ @PostFilter ineficiente
@PostFilter("filterObject.department == authentication.principal.department")
@GetMapping("/employees")
public List<Employee> getEmployees() {
    return employeeRepository.findAll(); // Busca TODOS
}

// ✅ Filtragem no Repository
@GetMapping("/employees")
public List<Employee> getEmployees() {
    String dept = getCurrentUserDepartment();
    return employeeRepository.findByDepartment(dept); // Busca apenas necessários
}
```

### Padrão Service com Contexto de Segurança

```java
// ❌ @PostFilter com lógica complexa
@PostFilter("@complexSecurityService.evaluate(authentication, filterObject)")
@GetMapping("/complex-data")
public List<ComplexData> getComplexData() {
    return dataService.findAll(); // Busca tudo, filtra depois
}

// ✅ Service com contexto de segurança
@GetMapping("/complex-data")
public List<ComplexData> getComplexData() {
    SecurityContext context = getCurrentSecurityContext();
    return dataService.findWithSecurityContext(context); // Filtra na origem
}
```

## Padrões Recomendados

### ✅ Use @PostFilter para: Filtragem Baseada em Conteúdo

```java
@PostFilter("filterObject.confidentialityLevel <= authentication.principal.clearanceLevel")
@GetMapping("/classified-documents")
public List<Document> getClassifiedDocuments() {
    // Conteúdo dos documentos determina nível de confidencialidade
    // Não é possível filtrar sem carregar o documento
    return documentService.loadAllClassifiedDocs();
}
```

### ✅ Use @PostFilter para: Resultados Dinâmicos

```java
@PostFilter("@dynamicPermissionService.hasPermission(authentication, filterObject)")
@GetMapping("/dynamic-content")
public List<Content> getDynamicContent() {
    // Permissões mudam baseadas em regras de negócio complexas
    // Mais simples filtrar após carregar o conteúdo
    return contentService.generateDynamicContent();
}
```

### ❌ Evite @PostFilter para: Operações Custosas

```java
// EVITE: Processamento caro para todos os itens
@PostFilter("filterObject.belongsToUser(authentication.name)")
@GetMapping("/expensive-reports")
public List<Report> getExpensiveReports() {
    // Gera todos os relatórios (muito caro!)
    return reportService.generateAllReports(); // ❌ Ineficiente
}

// PREFIRA: Geração seletiva
@GetMapping("/expensive-reports")
public List<Report> getExpensiveReports() {
    String userId = getCurrentUserId();
    return reportService.generateReportsForUser(userId); // ✅ Eficiente
}
```

## Debugging e Troubleshooting

### Problema 1: @PostFilter parece não funcionar

```java
// ❌ Erro comum: filterObject incorreto
@PostFilter("filterObject.userId == authentication.name")
public List<String> getUsernames() {
    return List.of("john", "admin", "other"); // Strings, não objetos!
}

// ✅ Correção
@PostFilter("filterObject == authentication.name")
public List<String> getUsernames() {
    return List.of("john", "admin", "other"); // filterObject é a String
}
```

### Problema 2: Performance inesperadamente lenta

```java
// ❌ @PostFilter custosa
@PostFilter("@heavyService.checkPermission(authentication, filterObject)")
public List<Item> getItems() {
    List<Item> items = itemService.findAll(); // 10.000 itens
    // checkPermission chamado 10.000 vezes!
    return items;
}

// ✅ Otimização: cache de permissões
@PostFilter("@optimizedSecurityService.hasPermission(authentication, filterObject)")
public List<Item> getItems() {
    List<Item> items = itemService.findAll();
    // Service usa cache interno para evitar cálculos repetidos
    return items;
}
```

### Logs Úteis para Debug

```java
@PostFilter("filterObject.owner == authentication.name")
@GetMapping("/debug-resources")
public List<Resource> getDebugResources() {
    List<Resource> resources = resourceService.fetchResources(
        List.of("john_doc1", "admin_doc2", "john_doc3")
    );
    
    System.out.println("Before @PostFilter: " + resources.size() + " items");
    // @PostFilter aplicado aqui
    // Após return: System.out.println seria útil, mas não é possível
    
    return resources;
}
```

Para debug de @PostFilter, use logs no serviço ou interceptors.

## Métricas e Monitoramento

### Métricas Importantes para @PostFilter

```java
@Component
public class PostFilterMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void onPostFilterExecution(PostFilterEvent event) {
        // Monitora eficiência da filtragem
        Timer.Sample sample = Timer.start(meterRegistry);
        
        int totalItems = event.getTotalItems();
        int filteredItems = event.getFilteredItems();
        double efficiency = (double) filteredItems / totalItems;
        
        // Alerta se eficiência for muito baixa (< 10%)
        if (efficiency < 0.1) {
            log.warn("@PostFilter with low efficiency: {}% for {}", 
                efficiency * 100, event.getMethodName());
        }
        
        sample.stop(Timer.builder("postfilter.execution")
            .tag("method", event.getMethodName())
            .tag("efficiency", efficiency > 0.5 ? "good" : "poor")
            .register(meterRegistry));
    }
}
```

## Testes Práticos

### Setup de Teste com curl:

```bash
# 1. @PostFilter - Resultado filtrado
curl -X GET -u john:password http://localhost:8080/api/resources/post-filter
# Logs mostram: Fetching ALL resources: [john_resource1, other_resource2, john_resource3]
# Resultado: Apenas recursos do John [{owner: "john", content: "..."}]

# 2. @PreFilter vs @PostFilter - Comparação de logs
curl -X POST -u john:password \
  -H "Content-Type: application/json" \
  -d '["john_doc1", "admin_doc2", "john_doc3"]' \
  http://localhost:8080/api/resources/filter
# @PreFilter - Log: Fetching resources for IDs: [john_doc1, john_doc3]

curl -X GET -u john:password http://localhost:8080/api/resources/post-filter
# @PostFilter - Log: Fetching ALL resources: [john_resource1, other_resource2, john_resource3]
```

### Comparação de Performance nos Logs:

#### @PreFilter (Eficiente):
```
[DEBUG] @PreFilter applied: 3 → 2 items
[DEBUG] Fetching resources for IDs: [john_doc1, john_doc3]
[DEBUG] Processing 2 items
[DEBUG] Response: 2 items returned
```

#### @PostFilter (Menos Eficiente):
```
[DEBUG] Fetching ALL resources: [john_resource1, other_resource2, john_resource3]
[DEBUG] Processing 3 items
[DEBUG] @PostFilter applied: 3 → 2 items
[DEBUG] Response: 2 items returned
```

## Resumo das Recomendações

### Decisão: @PreFilter vs @PostFilter

```
📝 Checklist de Decisão:

□ Posso controlar a entrada do método?
  ✅ Sim → Considere @PreFilter
  ❌ Não → @PostFilter pode ser necessário

□ O processamento é custoso?
  ✅ Sim → @PreFilter (otimiza performance)  
  ❌ Não → @PostFilter é aceitável

□ A filtragem depende do resultado processado?
  ✅ Sim → @PostFilter é necessário
  ❌ Não → @PreFilter é mais eficiente

□ O método tem efeitos colaterais?
  ✅ Sim → @PreFilter (evita efeitos indevidos)
  ❌ Não → Ambos são seguros

□ Espero filtrar > 50% dos itens?
  ✅ Sim → @PostFilter é ineficiente
  ❌ Não → @PostFilter é aceitável
```

### Para Novos Projetos:

1. **Primeira opção**: Filtragem no Repository/Service
2. **Segunda opção**: `@PreFilter` para otimizar entrada
3. **Terceira opção**: `@PreAuthorize` para controle simples
4. **Última opção**: `@PostFilter` apenas quando necessário

### Para APIs REST:

| Endpoint | Recomendação | Motivo |
|----------|--------------|--------|
| **GET /items** | Repository filter | Melhor performance |
| **GET /items/:id** | `@PreAuthorize` | Controle simples |
| **POST /items/batch** | `@PreFilter` | Otimiza processamento |
| **GET /processed-data** | `@PostFilter` | Filtragem pós-processamento |

### Checklist de Performance:

- [ ] Medi o impacto de performance da @PostFilter?
- [ ] Considerou alternativas mais eficientes?
- [ ] A filtragem é realmente necessária após o processamento?
- [ ] Implementei cache para operações de verificação custosas?
- [ ] Adicionei métricas para monitorar eficiência?

## Conclusão

`@PostFilter` é uma ferramenta poderosa mas que deve ser usada com cuidado. Sua principal vantagem é a capacidade de filtrar resultados baseados no conteúdo processado, mas sua principal desvantagem é o impacto na performance.

**Regras de Ouro**: 

- **Filtragem na Origem** → Repository/Service (Melhor)
- **Controle de Entrada** → `@PreFilter` (Bom) 
- **Controle de Acesso** → `@PreAuthorize` (Bom)
- **Filtragem de Resultado** → `@PostFilter` (Usar com cuidado)

**Pergunta-chave**: Você precisa processar todos os dados para depois decidir o que mostrar, ou pode decidir antecipadamente o que processar?

A resposta determina se `@PostFilter` é a escolha certa para seu caso de uso.