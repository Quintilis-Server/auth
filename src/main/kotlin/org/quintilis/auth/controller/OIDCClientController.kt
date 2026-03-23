package org.quintilis.auth.controller

import org.quintilis.auth.dto.ClientSettingsDTO
import org.quintilis.auth.dto.OIDCClientDTO
import org.quintilis.auth.dto.TokenSettingsDTO
import org.quintilis.auth.entities.OIDCClient
import org.quintilis.auth.extensions.toClientSettings
import org.quintilis.auth.extensions.toTokenSettings
import org.quintilis.auth.service.OIDCService
import org.quintilis.common.controller.BaseController
import org.quintilis.common.response.ApiResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/oidc")
class OIDCClientController(
    private val service: OIDCService
): BaseController<OIDCClient, String, OIDCClientDTO, OIDCClientController.NewOIDCClient>(service) {

    data class NewOIDCClient(
        val clientId: String,
        val clientSecret: String,
        val clientName: String,
        val clientAuthenticationMethods: List<String> = listOf("client_secret_basic"),
        val authorizationGrantTypes: List<String> = listOf("authorization_code", "refresh_token"),
        val redirectUris: List<String>,
        val postLogoutRedirectUris: List<String> = emptyList(),
        val scopes: List<String> = listOf("openid"),
        val clientSettings: ClientSettingsDTO = ClientSettingsDTO(),
        val tokenSettings: TokenSettingsDTO = TokenSettingsDTO()
    )

    data class OIDCMetadataDTO(
        val grantTypes: List<String>,
        val authenticationMethods: List<String>,
        val scopes: List<String>
    )

    @GetMapping("/metadata")
    fun getMetadata(): ApiResponse<OIDCMetadataDTO> {
        return ApiResponse.success(OIDCMetadataDTO(
            grantTypes = listOf(
                "authorization_code",
                "refresh_token",
                "client_credentials",
                "device_code"
            ),
            authenticationMethods = listOf(
                "client_secret_basic",
                "client_secret_post",
                "private_key_jwt",
                "none"
            ),
            scopes = listOf(
                "openid",
                "read_profile",
                "offline_access",
                "profile",
                "email"
            )
        ))
    }
}