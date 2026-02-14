package org.quintilis.auth.listener

import org.quintilis.common.entities.auth.LoginLog
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.LoginLogRepository
import org.quintilis.common.repositories.UserRepository
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class AuthenticationEvents(
    private val loginLogRepository: LoginLogRepository,
    private val userRepository: UserRepository
) {

    @EventListener
    fun onSuccess(event: AuthenticationSuccessEvent) {
        val authentication = event.authentication
        val principal = authentication.principal
        val details = authentication.details as? WebAuthenticationDetails

        // Tenta descobrir o identificador (pode ser email, username ou UUID)
        val identifier = when (principal) {
            is OAuth2User -> principal.attributes["email"]?.toString() ?: principal.name
            else -> authentication.name
        }

        // Busca o usuário no banco
        var user: User? = userRepository.findByEmail(identifier) 
            ?: userRepository.findByUsername(identifier)

        // Se não achou e parece um UUID, tenta buscar pelo ID
        if (user == null) {
            try {
                val uuid = UUID.fromString(identifier)
                user = userRepository.findById(uuid).orElse(null)
            } catch (e: IllegalArgumentException) {
                // Não é UUID, ignora
            }
        }

        if (user != null) {
            val log = LoginLog().apply {
                this.user = user
                this.timestamp = Instant.now()
                this.ipAddress = details?.remoteAddress ?: "unknown"
                
                this.loginMethod = when (authentication) {
                    is OAuth2LoginAuthenticationToken -> authentication.clientRegistration.registrationId.uppercase() // GOOGLE, MICROSOFT
                    else -> "LOCAL"
                }
            }
            loginLogRepository.save(log)
            println("Login log salvo para: ${user.username} via ${log.loginMethod}")
        } else {
            println("AVISO: Login bem sucedido mas usuário não encontrado no banco para log: $identifier")
        }
    }
}
