package org.quintilis.auth.config

import org.quintilis.auth.service.CustomOAuth2Service
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
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
    private val customOAuth2Service: CustomOAuth2Service
) {

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/auth/register", "/error", "/login", "/css/**", "/js/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login").permitAll() // Usa nossa página customizada
            }
            .oauth2Login { oauth ->
                oauth.loginPage("/login") // Também usa nossa página para login social
                oauth.userInfoEndpoint { it.userService(customOAuth2Service) }
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .csrf { it.disable() }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
