package org.quintilis.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.quintilis.auth.controller.OIDCClientController
import org.quintilis.auth.dto.OIDCClientDTO
import org.quintilis.auth.entities.OIDCClient
import org.quintilis.auth.extensions.toClientSettings
import org.quintilis.auth.extensions.toJson
import org.quintilis.auth.extensions.toTokenSettings
import org.quintilis.auth.repositories.OIDCRepository
import org.quintilis.common.exception.NotFoundException
import org.quintilis.common.service.BaseService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID
import kotlin.reflect.KProperty1

@Service
class OIDCService(
    private val repository: OIDCRepository,
    private val registeredClientRepository: RegisteredClientRepository,
    private val passwordEncoder: PasswordEncoder
): BaseService<OIDCClient, String, OIDCClientDTO, OIDCClientController.NewOIDCClient>(repository = repository) {
    fun getMapper(){

    }
    override fun newDTOToEntity(newDTO: OIDCClientController.NewOIDCClient): OIDCClient {
        val registered = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(newDTO.clientId)
            .clientSecret(passwordEncoder.encode(newDTO.clientSecret))
            .clientName(newDTO.clientName)
            .clientAuthenticationMethods { m ->
                newDTO.clientAuthenticationMethods.forEach { m.add(ClientAuthenticationMethod(it)) }
            }
            .authorizationGrantTypes { t ->
                newDTO.authorizationGrantTypes.forEach { t.add(AuthorizationGrantType(it)) }
            }
            .redirectUris { it.addAll(newDTO.redirectUris) }
            .postLogoutRedirectUris { it.addAll(newDTO.postLogoutRedirectUris) }
            .scopes { it.addAll(newDTO.scopes) }
            .clientSettings(newDTO.clientSettings.toClientSettings())
            .tokenSettings(newDTO.tokenSettings.toTokenSettings())
            .build()

        registeredClientRepository.save(registered)

        return repository.findById(registered.id).orElseThrow()
    }

    override fun getSearchFields(): List<KProperty1<OIDCClient, *>> {
        return listOf(
            OIDCClient::clientId,
            OIDCClient::clientName,
            OIDCClient::scope
        )
    }

    override fun updateEntityFromDTO(dto: OIDCClientDTO, entity: OIDCClient) {
        val existing = registeredClientRepository.findByClientId(entity.clientId!!)
            ?: throw NotFoundException("Cliente não encontrado")

        val updated = RegisteredClient.from(existing)
            .clientName(dto.clientName)
            .redirectUris { uris ->
                uris.clear()
                uris.addAll(dto.redirectUris)
            }
            .scopes { scopes ->
                scopes.clear()
                scopes.addAll(dto.scopes)
            }
            .clientSettings(dto.clientSettings.toClientSettings())
            .tokenSettings(dto.tokenSettings.toTokenSettings())
            .build()

        registeredClientRepository.save(updated)
    }

    @Cacheable("all_oidc_clients")
    override fun findAll(search: String?, pageable: Pageable, includeInactive: Boolean): Page<OIDCClientDTO> {
        return super.findAll(search, pageable, includeInactive)
    }

    @Cacheable("oidc_client", key = "#id")
    override fun findById(id: String, includeInactive: Boolean): OIDCClient {
        return super.findById(id, includeInactive)
    }

    @CacheEvict("oidc_client", "all_oidc_clients", allEntries = true)
    override fun update(dto: OIDCClientDTO, id: String): OIDCClientDTO {
        return super.update(dto, id)
    }

    @CacheEvict("oidc_client", "all_oidc_clients", allEntries = true)
    override fun create(dto: OIDCClientController.NewOIDCClient): OIDCClientDTO {
        return super.create(dto)
    }

    @CacheEvict("oidc_client", "all_oidc_clients", allEntries = true)
    override fun delete(id: String, hardDelete: Boolean): OIDCClientDTO {
        return super.delete(id, hardDelete)
    }
}