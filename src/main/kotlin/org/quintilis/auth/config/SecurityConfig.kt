package org.quintilis.auth.config

import org.quintilis.auth.handler.OAuth2SuccessHandler
import org.quintilis.auth.service.CustomOidcUserService
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
open class SecurityConfig( // Adicionado 'open'
    private val customOidcUserService: CustomOidcUserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/", "/index.html", "/static/**", "/assets/**",
                        "/login", "/register",
                        "/auth/register", "/error",
                        "/favicon.ico"
                    ).permitAll()
                auth.anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/profile", true)
                    .permitAll()
            }
            .oauth2Login { oauth ->
                oauth.loginPage("/login")
                oauth.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(customOidcUserService)
                }
                oauth.successHandler(oAuth2SuccessHandler)
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
