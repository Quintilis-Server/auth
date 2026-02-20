package org.quintilis.auth.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID
import org.quintilis.auth.config.properties.ClientSettingsProperties
import org.quintilis.auth.config.properties.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ClientSettingsProperties::class, CorsProperties::class)
open class AuthorizationServerConfig {

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer()
        authorizationServerConfigurer.oidc(Customizer.withDefaults())

        http.securityMatcher(authorizationServerConfigurer.endpointsMatcher)
                .authorizeHttpRequests { authorize -> authorize.anyRequest().authenticated() }
                .csrf { csrf ->
                    csrf.ignoringRequestMatchers(authorizationServerConfigurer.endpointsMatcher)
                }
                .cors(Customizer.withDefaults()) // Habilita CORS
                .exceptionHandling { exceptions ->
                    exceptions.defaultAuthenticationEntryPointFor(
                            LoginUrlAuthenticationEntryPoint("/login"),
                            MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                    )
                }
                .oauth2ResourceServer { resourceServer ->
                    resourceServer.jwt(Customizer.withDefaults())
                }
                .with(authorizationServerConfigurer, Customizer.withDefaults())

        return http.build()
    }

    @Bean
    fun registeredClientRepository(
            clientSettings: ClientSettingsProperties,
            passwordEncoder: PasswordEncoder
    ): RegisteredClientRepository {
        val registeredClients =
                clientSettings.clients.map { client ->
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(client.clientId)
                            .clientSecret(passwordEncoder.encode(client.clientSecret))
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                            )
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                            .redirectUris { uris -> uris.addAll(client.redirectUris) }
                            .scopes { scopes -> scopes.addAll(client.scopes) }
                            .clientSettings(
                                    ClientSettings.builder()
                                            .requireAuthorizationConsent(false)
                                            .requireProofKey(
                                                    false
                                            ) // Desabilita a exigência de PKCE
                                            .build()
                            )
                            .build()
                }
        return InMemoryRegisteredClientRepository(registeredClients)
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = generateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey =
                RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID(UUID.randomUUID().toString())
                        .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    private fun generateRsaKey(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().build()
    }

    @Bean
    fun corsConfigurationSource(corsProperties: CorsProperties): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns =
                corsProperties.allowedOrigins // Usa allowedOriginPatterns para suportar wildcards
        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
