package org.quintilis.auth.config

import org.quintilis.auth.service.CustomUserDetailsService
import org.quintilis.common.repositories.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.util.UUID

@Configuration
class CustomTokenCustomizer(
    private val userRepository: UserRepository
) {

    @Bean
    fun tokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context ->
            val principal = context.getPrincipal<Authentication>()
            val userType = context.tokenType.value
            
            if ("access_token" == userType) {
                // Tenta achar o usuário pelo nome (que pode ser o ID ou email/username)
                val identifier = principal.name
                val user = userRepository.findByEmail(identifier) 
                    ?: userRepository.findByUsername(identifier)
                    ?: try { userRepository.findById(UUID.fromString(identifier)).orElse(null) } catch (e: Exception) { null }

                if (user != null) {
                    // Injeta a role no token
                    context.claims.claim("roles", listOf(user.role)) // Ex: ["ADMIN"] ou ["USER"]
                    context.claims.claim("user_id", user.id.toString())
                    context.claims.claim("username", user.username)
                }
            }
        }
    }
}
