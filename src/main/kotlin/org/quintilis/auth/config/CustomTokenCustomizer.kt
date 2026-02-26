package org.quintilis.auth.config

import java.util.UUID
import org.quintilis.common.repositories.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

@Configuration
open class CustomTokenCustomizer( // Adicionado 'open'
private val userRepository: UserRepository) {

    @Bean
    fun tokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context ->
            val principal = context.getPrincipal<Authentication>()
            val userType = context.tokenType.value

            if ("access_token" == userType) {
                val identifier = principal.name
                val user =
                        userRepository.findByEmail(identifier)
                                ?: userRepository.findByUsername(identifier)
                                        ?: try {
                                    userRepository
                                            .findById(UUID.fromString(identifier))
                                            .orElse(null)
                                } catch (e: Exception) {
                                    null
                                }

                if (user != null) {
                    val roles = user.roles.mapNotNull { it.name }
                    val permissions =
                            user.roles.flatMap { it.permissions }.mapNotNull { it.name }.distinct()

                    context.claims.claim("roles", roles)
                    context.claims.claim("permissions", permissions)
                    context.claims.claim("user_id", user.id.toString())
                    context.claims.claim("username", user.username)
                }
            }
        }
    }
}
