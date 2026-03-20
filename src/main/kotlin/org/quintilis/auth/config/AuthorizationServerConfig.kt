package org.quintilis.auth.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.util.Base64
import java.util.UUID
import org.quintilis.auth.config.properties.ClientSettingsProperties
import org.quintilis.auth.config.properties.CorsProperties
import org.quintilis.auth.handler.OAuth2SuccessHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ClientSettingsProperties::class, CorsProperties::class)
open class AuthorizationServerConfig {

    @Value("\${quintilis.auth.issuer-url}")
    private lateinit var issuerUrl: String

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(
        http: HttpSecurity,
        oAuth2SuccessHandler: OAuth2SuccessHandler,  // injeta aqui
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityFilterChain {
        val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer()
        authorizationServerConfigurer.oidc(Customizer.withDefaults())

        http.securityMatcher(authorizationServerConfigurer.endpointsMatcher)
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.anyRequest().authenticated()
            }
            .csrf { csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.endpointsMatcher) }
            .cors { it.configurationSource(corsConfigurationSource) }
            .exceptionHandling { exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(oAuth2SuccessHandler)  // <-- handler correto aqui
                    .permitAll()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        LoginUrlAuthenticationEntryPoint("/login"),
                        MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                    )
                    .authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/login"))
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .with(authorizationServerConfigurer, Customizer.withDefaults())

        return http.build()
    }

    @Bean
    fun registeredClientRepository(
        jdbcTemplate: JdbcTemplate, // Injetando o banco de dados
        clientSettings: ClientSettingsProperties,
        passwordEncoder: PasswordEncoder
    ): RegisteredClientRepository {

        // 1. Cria o repositório baseado no JDBC (Banco de Dados)
        val repository = JdbcRegisteredClientRepository(jdbcTemplate)

        // 2. Itera sobre os clientes que vieram do application.yml
        clientSettings.clients.forEach { client ->

            // 3. Só salva no banco se o cliente ainda não existir
            if (repository.findByClientId(client.clientId) == null) {
                val registeredClient = RegisteredClient.withId(
                    UUID.nameUUIDFromBytes(client.clientId.toByteArray()).toString()
                )
                    .clientId(client.clientId)
                    .clientSecret(passwordEncoder.encode(client.clientSecret))
                    .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                    )
                    .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                    )
                    .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN
                    )
                    .redirectUris { uris -> uris.addAll(client.redirectUris) }
                    .scopes { scopes -> scopes.addAll(client.scopes) }
                    .clientSettings(
                        ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(false) // Desabilita a exigência de PKCE
                            .build()
                    )
                    .tokenSettings(
                        TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .refreshTokenTimeToLive(Duration.ofDays(30))
                            .reuseRefreshTokens(true)
                            .build()
                    )
                    .build()

                // 4. Salva fisicamente na tabela oauth2_registered_client
                repository.save(registeredClient)
            }
        }

        // 5. Retorna o repositório JDBC em vez do InMemory
        return repository
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val keyPair = getOrCreateRsaKey()
        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        val rsaKey =
            RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("quintilis-auth-key")
                .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    private fun getOrCreateRsaKey(): KeyPair {
        val keyDir = File("./keys")
        val publicKeyFile = File(keyDir, "public.key")
        val privateKeyFile = File(keyDir, "private.key")

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey =
                keyFactory.generatePublic(
                    X509EncodedKeySpec(
                        Base64.getDecoder().decode(publicKeyFile.readText())
                    )
                ) as
                        RSAPublicKey
            val privateKey =
                keyFactory.generatePrivate(
                    PKCS8EncodedKeySpec(
                        Base64.getDecoder()
                            .decode(privateKeyFile.readText())
                    )
                ) as
                        RSAPrivateKey
            return KeyPair(publicKey, privateKey)
        }

        // Gera e salva novas chaves
        keyDir.mkdirs()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        publicKeyFile.writeText(Base64.getEncoder().encodeToString(keyPair.public.encoded))
        privateKeyFile.writeText(
            Base64.getEncoder().encodeToString(keyPair.private.encoded)
        )

        return keyPair
    }

    @Bean
    fun authorizationService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
    }

    @Bean
    fun authorizationConsentService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(
            jdbcTemplate,
            registeredClientRepository
        )
    }

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder()
            .issuer(issuerUrl)
            .build()
    }

    @Bean
    fun corsConfigurationSource(corsProperties: CorsProperties): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns =
            corsProperties
                .allowedOrigins // Usa allowedOriginPatterns para suportar wildcards
        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
