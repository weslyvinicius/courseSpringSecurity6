package com.academy.springsecurity6full.infrastructure.repository;

import com.academy.springsecurity6full.infrastructure.repository.entity.RegisteredClientEntity;
import com.academy.springsecurity6full.infrastructure.repository.mapper.RegisteredClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final JpaRegisteredClientRepository jpaRepository;
    private final RegisteredClientMapper mapper;

    @Override
    public void save(RegisteredClient registeredClient) {
        log.debug("Salvando cliente: {}", registeredClient.getClientId());

        try {
            RegisteredClientEntity entity = mapper.toEntity(registeredClient);
            jpaRepository.save(entity);
            log.info("Cliente {} salvo com sucesso!", registeredClient.getClientId());
        } catch (Exception e) {
            log.error("Erro ao salvar cliente {}: {}", registeredClient.getClientId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar cliente: " + registeredClient.getClientId(), e);
        }
    }

    @Override
    public RegisteredClient findById(String id) {
        log.debug("Buscando cliente por ID: {}", id);

        try {
            return jpaRepository.findById(id)
                    .map(mapper::toRegisteredClient)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar cliente por ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        log.debug("Buscando cliente por Client ID: {}", clientId);

        try {
            return jpaRepository.findByClientId(clientId)
                    .map(mapper::toRegisteredClient)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar cliente por Client ID {}: {}", clientId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Remove um cliente por ID
     *
     * @param id ID interno do cliente
     */
    public void deleteById(String id) {
        log.info("Removendo cliente com ID: {}", id);

        try {
            jpaRepository.deleteById(id);
            log.info("Cliente com ID {} removido com sucesso!", id);
        } catch (Exception e) {
            log.error("Erro ao remover cliente com ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Falha ao remover cliente com ID: " + id, e);
        }
    }

    /**
     * Remove um cliente por Client ID
     *
     * @param clientId Client ID público do cliente
     */
    public void deleteByClientId(String clientId) {
        log.info("Removendo cliente com Client ID: {}", clientId);

        try {
            jpaRepository.findByClientId(clientId)
                    .ifPresentOrElse(
                            client -> {
                                jpaRepository.deleteById(client.getId());
                                log.info("Cliente {} removido com sucesso!", clientId);
                            },
                            () -> log.warn("Cliente com Client ID {} não encontrado para remoção", clientId)
                    );
        } catch (Exception e) {
            log.error("Erro ao remover cliente {}: {}", clientId, e.getMessage(), e);
            throw new RuntimeException("Falha ao remover cliente: " + clientId, e);
        }
    }

    /**
     * Lista todos os clientes registrados
     *
     * @return lista de todos os clientes
     */
    public List<RegisteredClient> findAll() {
        log.debug("Listando todos os clientes registrados");

        try {
            List<RegisteredClient> clients = jpaRepository.findAll().stream()
                    .map(mapper::toRegisteredClient)
                    .collect(Collectors.toList());

            log.info("Encontrados {} clientes registrados", clients.size());
            return clients;
        } catch (Exception e) {
            log.error("Erro ao listar clientes: {}", e.getMessage(), e);
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }

    /**
     * Verifica se existe um cliente com o Client ID especificado
     *
     * @param clientId Client ID a ser verificado
     * @return true se existe, false caso contrário
     */
    public boolean existsByClientId(String clientId) {
        log.debug("Verificando existência do cliente: {}", clientId);

        try {
            boolean exists = jpaRepository.findByClientId(clientId).isPresent();
            log.debug("Cliente {} existe: {}", clientId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Erro ao verificar existência do cliente {}: {}", clientId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Atualiza um cliente existente
     *
     * @param registeredClient cliente com dados atualizados
     * @return cliente atualizado ou null se não encontrado
     */
    public RegisteredClient update(RegisteredClient registeredClient) {
        log.info("Atualizando cliente: {}", registeredClient.getClientId());

        try {
            // Verifica se o cliente existe
            if (!existsByClientId(registeredClient.getClientId())) {
                log.warn("Cliente {} não encontrado para atualização", registeredClient.getClientId());
                return null;
            }

            // Salva as alterações
            save(registeredClient);

            // Retorna o cliente atualizado
            return findByClientId(registeredClient.getClientId());
        } catch (Exception e) {
            log.error("Erro ao atualizar cliente {}: {}", registeredClient.getClientId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao atualizar cliente: " + registeredClient.getClientId(), e);
        }
    }

    /**
     * Conta o número total de clientes registrados
     *
     * @return número de clientes
     */
    public long count() {
        try {
            long count = jpaRepository.count();
            log.debug("Total de clientes registrados: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Erro ao contar clientes: {}", e.getMessage(), e);
            return 0;
        }
    }
}