package org.quintilis.auth.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.quintilis.auth.entities.OIDCClient
import org.quintilis.auth.extensions.toClientSettings
import org.quintilis.auth.extensions.toJson
import org.quintilis.auth.extensions.toTokenSettings
import org.quintilis.common.dto.BaseDTO
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration
import java.time.Instant
import java.util.UUID


data class OIDCClientDTO(
    val id: String? = null,
    val clientId: String,
    val clientIdIssuedAt: Instant,
    val clientSecret: String? = null,
    val clientSecretExpiresAt: Instant? = null,
    val clientName: String,
    val clientAuthenticationMethods: List<String>,
    val authorizationGrantTypes: List<String>,
    val redirectUris: List<String>,
    val postLogoutRedirectUris: List<String>,
    val scopes: List<String>,
    val clientSettings: ClientSettingsDTO,
    val tokenSettings: TokenSettingsDTO,
): BaseDTO<OIDCClient> {
    fun toRegisteredClient(passwordEncoder: PasswordEncoder, existingId: String, existingSecret: String?): RegisteredClient {
        val cs = clientSettings.toClientSettings()
        val ts = tokenSettings.toTokenSettings()

        return RegisteredClient.withId(existingId)
            .clientId(clientId)
            .clientSecret(
                if (!clientSecret.isNullOrBlank()) passwordEncoder.encode(clientSecret)
                else existingSecret ?: ""
            )
            .clientName(clientName)
            .clientAuthenticationMethods { methods ->
                clientAuthenticationMethods.forEach { methods.add(ClientAuthenticationMethod(it)) }
            }
            .authorizationGrantTypes { types ->
                authorizationGrantTypes.forEach { types.add(AuthorizationGrantType(it)) }
            }
            .redirectUris { uris -> uris.addAll(redirectUris) }
            .postLogoutRedirectUris { uris -> uris.addAll(postLogoutRedirectUris) }
            .scopes { s -> s.addAll(scopes) }
            .clientSettings(cs)
            .tokenSettings(ts)
            .build()
    }
    override fun toEntity(): OIDCClient {
        return OIDCClient().apply {
            this.clientId = this@OIDCClientDTO.clientId
            this.clientName = this@OIDCClientDTO.clientName
            this.clientSecret = this@OIDCClientDTO.clientSecret
            this.clientSecretExpiresAt = this@OIDCClientDTO.clientSecretExpiresAt
            this.clientAuthenticationMethods = this@OIDCClientDTO.clientAuthenticationMethods.joinToString(",")
            this.authorizationGrantTypes = this@OIDCClientDTO.authorizationGrantTypes.joinToString(",")
            this.redirectUris = this@OIDCClientDTO.redirectUris.joinToString(",")
            this.postLogOutRedirectUris = this@OIDCClientDTO.postLogoutRedirectUris.joinToString(",")
            this.scope = this@OIDCClientDTO.scopes.joinToString(",")
            this.clientSettings = this@OIDCClientDTO.clientSettings.toClientSettings().toJson()
            this.tokenSettings = this@OIDCClientDTO.tokenSettings.toTokenSettings().toJson()
        }
    }

    private fun getDurationHours(settings: TokenSettings, key: String): Long {
        return when (val value = settings.getSetting<Any>(key)) {
            is Duration -> value.toHours()
            is Double -> Duration.ofSeconds(value.toLong()).toHours()
            is Long -> Duration.ofSeconds(value).toHours()
            else -> 1L
        }
    }

    private fun getDurationMinutes(settings: TokenSettings, key: String): Long {
        return when (val value = settings.getSetting<Any>(key)) {
            is Duration -> value.toMinutes()
            is Double -> Duration.ofSeconds(value.toLong()).toMinutes()
            is Long -> Duration.ofSeconds(value).toMinutes()
            else -> 5L
        }
    }
}
