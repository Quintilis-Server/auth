package org.quintilis.auth.extensions

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.quintilis.auth.dto.ClientSettingsDTO
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator

fun getOAuth2ObjectMapper(): ObjectMapper {
    val mapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        // Força o Jackson a escrever e ler a propriedade "@class" no JSON
        val ptv: PolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()
        activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
    }
    return mapper
}

fun ClientSettings.toDTO() = ClientSettingsDTO(
    requireProofKey = this.getSetting("settings.client.require-proof-key") ?: false,
    requireAuthorizationConsent = this.getSetting("settings.client.require-authorization-consent") ?: false
)
fun ClientSettings.toJson(): String {
    // Usamos o mapper com suporte a polimorfismo
    return getOAuth2ObjectMapper().writeValueAsString(this.settings)
}

fun ClientSettingsDTO.toClientSettings() = ClientSettings.builder()
    .requireProofKey(this.requireProofKey)
    .requireAuthorizationConsent(this.requireAuthorizationConsent)
    .build()

fun String?.toClientSettings(): ClientSettings {
    if (this.isNullOrBlank()) {
        return ClientSettings.builder().build()
    }

    return try {
        // Tenta ler com o mapper polimórfico (se vier com @class)
        val map = getOAuth2ObjectMapper().readValue(this, object : TypeReference<Map<String, Any>>() {})
        ClientSettings.withSettings(map).build()
    } catch (e: Exception) {
        // Se falhar (ex: formato de JSON simples sem @class), usa um mapper padrão
        try {
            val simpleMapper = ObjectMapper().apply { registerModule(KotlinModule.Builder().build()) }
            val map = simpleMapper.readValue(this, object : TypeReference<Map<String, Any>>() {})
            ClientSettings.withSettings(map).build()
        } catch (innerE: Exception) {
            // Em último caso, retorna configurações padrão para não estourar a API
            ClientSettings.builder().build()
        }
    }
}
