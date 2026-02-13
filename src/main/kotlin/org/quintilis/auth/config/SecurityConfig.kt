package org.quintilis.auth.config

import org.quintilis.auth.handler.OAuth2SuccessHandler
import org.quintilis.auth.service.CustomOAuth2Service
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2Service: CustomOAuth2Service,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Essencial desativar para APIs REST/React
            .cors { } // Ativa o CORS para não dar erro no navegador
            .authorizeHttpRequests { auth ->
                // Libera as rotas de login (Local e Social)
                auth.requestMatchers("/auth/**", "/login/**", "/oauth2/**", "/error").permitAll()

                // Qualquer outra requisição neste microserviço exige autenticação
                auth.anyRequest().authenticated()
            }
//            .oauth2Login { oauth ->
//                // Diz pro Spring usar seu serviço para salvar no banco (Postgres)
//                oauth.userInfoEndpoint { endpoint ->
//                    endpoint.userService(customOAuth2Service)
//                }
//                // Diz pro Spring chamar o Handler quando o Google/Microsoft retornar sucesso
//                oauth.successHandler(oAuth2SuccessHandler)
//            }

        return http.build()
    }

    // Configura o BCrypt para verificar a senha no AuthController
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // Expõe o AuthenticationManager para podermos injetá-lo no AuthController
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}