package org.quintilis.auth.config

import org.quintilis.auth.handler.OAuth2SuccessHandler
import org.quintilis.auth.service.CustomOidcUserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.savedrequest.NullRequestCache
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                auth
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/**",
                        "/assets/**",
                        "/login",
                        "/register",
                        "/auth/register",
                        "/error",
                        "/favicon.ico"
                    ).permitAll()
                    // Permite visualização pública de listas (GET)
                    .requestMatchers(HttpMethod.GET, "/roles/**", "/users/**", "/permissions/**", "/routes/**").permitAll()
                    // Exige autenticação para qualquer outra operação dentro de /auth (POST, PUT, DELETE)
                    .requestMatchers("/auth/**").authenticated()
                    .anyRequest().authenticated()
            }
            .cors { it.disable() }
//                .cors { cors ->
//                    cors.configurationSource { request ->
//                        CorsConfiguration().apply {
//                            allowedOriginPatterns = listOf("http://localhost:*", "https://*.quintilis.org")
//                            allowedMethods = listOf("*")
//                            allowedHeaders = listOf("*")
//                            allowCredentials = true
//                        }
//                    }
//                }
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
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
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
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val roles = jwt.getClaimAsStringList("roles")?.map{"ROLE_$it"} ?: emptyList()
            val permissions = jwt.getClaimAsStringList("permissions") ?: emptyList()
            (roles + permissions).map { SimpleGrantedAuthority(it) }
        }
        return converter
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
