package org.quintilis.auth.config

import org.quintilis.auth.handler.OAuth2SuccessHandler
import org.quintilis.auth.service.CustomOidcUserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.savedrequest.NullRequestCache

@Configuration
@EnableWebSecurity
open class SecurityConfig(
        private val customOidcUserService: CustomOidcUserService,
        private val oAuth2SuccessHandler: OAuth2SuccessHandler,
        @Value("\${frontend.url:http://localhost:3000}") private val frontendUrl: String
) {

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .authorizeHttpRequests { auth ->
                    auth.requestMatchers(
                                    "/",
                                    "/index.html",
                                    "/static/**",
                                    "/assets/**",
                                    "/login",
                                    "/register",
                                    "/auth/register",
                                    "/error",
                                    "/favicon.ico"
                            )
                            .permitAll()
                    auth.anyRequest().authenticated()
                }
                .cors(Customizer.withDefaults()) // Habilita CORS
                .formLogin { form ->
                    form.loginPage("/login")
                            .loginProcessingUrl("/login")
                            .successHandler(oAuth2SuccessHandler) // Usa o handler inteligente
                            .permitAll()
                }
                .oauth2Login { oauth ->
                    oauth.loginPage("/login")
                    oauth.userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(customOidcUserService)
                    }
                    oauth.successHandler(oAuth2SuccessHandler) // Usa o handler inteligente
                }
                .logout { logout ->
                    logout.logoutUrl("/logout")
                    logout.invalidateHttpSession(true)
                    logout.clearAuthentication(true)
                    logout.deleteCookies("JSESSIONID")
                    logout.logoutSuccessHandler { request, response, _ ->
                        val redirectUri = request.getParameter("redirect_uri") ?: frontendUrl
                        response.sendRedirect(redirectUri)
                    }
                    logout.permitAll()
                }
                .csrf { it.disable() }
                .requestCache { it.requestCache(NullRequestCache()) }

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
