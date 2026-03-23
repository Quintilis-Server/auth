package org.quintilis.auth.extensions

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.quintilis.auth.dto.TokenSettingsDTO
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration
import org.quintilis.auth.extensions.toDTO
import org.quintilis.auth.extensions.toJson

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

fun TokenSettings.toDTO() = TokenSettingsDTO(
    reuseRefreshTokens = this.getSetting("settings.token.reuse-refresh-tokens") ?: true,
    accessTokenTtlHours = getDurationHours(this, "settings.token.access-token-time-to-live"),
    refreshTokenTtlHours = getDurationHours(this, "settings.token.refresh-token-time-to-live"),
    authorizationCodeTtlMinutes = getDurationMinutes(this, "settings.token.authorization-code-time-to-live")
)

fun TokenSettings.toJson(): String {
    // Usamos o mapper com suporte a polimorfismo
    return getOAuth2ObjectMapper().writeValueAsString(this.settings)
}

fun TokenSettingsDTO.toTokenSettings() = TokenSettings.builder()
    .reuseRefreshTokens(this.reuseRefreshTokens)
    .accessTokenTimeToLive(Duration.ofHours(this.accessTokenTtlHours))
    .refreshTokenTimeToLive(Duration.ofHours(this.refreshTokenTtlHours))
    .authorizationCodeTimeToLive(Duration.ofMinutes(this.authorizationCodeTtlMinutes))
    .build()

fun String?.toTokenSettings(): TokenSettings {
    if (this.isNullOrBlank()) {
        return TokenSettings.builder().build()
    }

    return try {
        // Tenta ler com o mapper polimórfico (se vier com @class)
        val map = getOAuth2ObjectMapper().readValue(this, object : TypeReference<Map<String, Any>>() {})
        TokenSettings.withSettings(map).build()
    } catch (e: Exception) {
        // Se falhar, usa um mapper padrão
        try {
            val simpleMapper = ObjectMapper().apply { registerModule(KotlinModule.Builder().build()) }
            val map = simpleMapper.readValue(this, object : TypeReference<Map<String, Any>>() {})
            TokenSettings.withSettings(map).build()
        } catch (innerE: Exception) {
            TokenSettings.builder().build()
        }
    }
}