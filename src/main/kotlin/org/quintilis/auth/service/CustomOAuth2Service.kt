package org.quintilis.auth.service

import java.util.UUID
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2Service(private val userRepository: UserRepository) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2Service::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User? {
        println("--- INICIANDO LOGIN SOCIAL ---")
        val oauthUser = super.loadUser(userRequest)
        val provider = userRequest?.clientRegistration?.registrationId
        println("Provider: $provider")

        val attributes = oauthUser.attributes
        println("Atributos recebidos: $attributes")

        val email = attributes["email"]?.toString() ?: ""
        val name = attributes["name"]?.toString() ?: "Usuario"
        val providerId = attributes["sub"]?.toString() ?: attributes["id"]?.toString() ?: ""

        println("Email: $email | Name: $name | ProviderID: $providerId")

        if (email.isNullOrEmpty()) {
            logger.error("ERRO: Email não retornado pelo provedor.")
            throw OAuth2AuthenticationException("Email not provided by OAuth2 provider")
        }

        // 1. Tenta buscar pelo ID Social primeiro (Login rápido)
        var user: User? =
                when (provider) {
                    "google" -> userRepository.findByGoogleId(providerId)
                    "microsoft" -> userRepository.findByMicrosoftId(providerId)
                    else -> null
                }

        if (user != null) {
            println("Usuário encontrado pelo ID Social: ${user.username}")
        }

        if (user == null) {
            logger.info("Usuário não encontrado pelo ID Social. Buscando por email: $email")
            user = userRepository.findByEmail(email)

            if (user != null) {
                logger.info("Conta local encontrada! Vinculando ao provedor $provider...")
                when (provider) {
                    "google" -> user.googleId = providerId
                    "microsoft" -> user.microsoftId = providerId
                }
                user = userRepository.save(user)
                logger.info("Conta vinculada com sucesso.")
            }
        }

        if (user == null) {
            println("Usuário não existe. Criando nova conta...")
            try {
                val newUser = User()
                newUser.id = UUID.randomUUID() // Forçando ID se o banco não gerar
                var desiredUsername = name
                if (userRepository.findByUsername(desiredUsername) != null) {
                    desiredUsername = name + "_" + (1000..9999).random()
                }
                newUser.username = desiredUsername
                newUser.email = email
                newUser.role = "USER"
                newUser.isVerified = true // Login social geralmente já é verificado

                if (provider == "google") {
                    newUser.googleId = providerId
                }
                if (provider == "microsoft") {
                    newUser.microsoftId = providerId
                }

                user = userRepository.save(newUser)
                println("Novo usuário salvo no banco: ${user.id} - ${user.username}")
            } catch (e: Exception) {
                println("ERRO AO SALVAR USUÁRIO: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }

        return DefaultOAuth2User(
                oauthUser.authorities,
                attributes.plus("internal_user_id" to user.id.toString()),
                "email" // Define qual atributo é o "nome principal"
        )
    }
}
