package org.quintilis.auth.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.quintilis.auth.dto.OIDCClientDTO
import org.quintilis.auth.extensions.toClientSettings
import org.quintilis.auth.extensions.toDTO
import org.quintilis.auth.extensions.toTokenSettings
import org.quintilis.common.entities.StringEntity
import org.quintilis.common.entities.UuidEntity
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "oauth2_registered_client", schema = "auth")
class OIDCClient : StringEntity<OIDCClientDTO>(){


    @NotNull
    @Column(name = "client_id")
    var clientId: String? = null

    @NotNull
    @Column(name = "client_id_issued_at")
    override var createdAt: Instant? = Instant.now()

    @Column(name = "client_secret")
    var clientSecret: String? = null

    @Column(name = "client_secret_expires_at")
    var clientSecretExpiresAt: Instant? = null

    @NotNull
    @Column(name = "client_name")
    var clientName: String? = null

    @NotNull
    @Column(name = "client_authentication_methods")
    var clientAuthenticationMethods: String? = null

    @NotNull
    @Column(name = "authorization_grant_types")
    var authorizationGrantTypes: String? = null

    @Column(name = "redirect_uris", length = 1000)
    var redirectUris: String? = null

    @Column(name = "post_logout_redirect_uris", length = 1000)
    var postLogOutRedirectUris: String? = null

    @Column(name = "scopes", length = 1000)
    var scope: String? = null

    @Column(name = "client_settings", length = 4000)
    var clientSettings: String? = null

    @Column(name = "token_settings", length = 4000)
    var tokenSettings: String? = null

    override fun toDTO(): OIDCClientDTO {
        val cs = this.clientSettings?.toClientSettings() ?: ClientSettings.builder().build()
        val ts = this.tokenSettings?.toTokenSettings() ?: TokenSettings.builder().build()

        return OIDCClientDTO(
            id = this.id,
            clientId = this.clientId ?: "",
            clientIdIssuedAt = this.createdAt ?: Instant.now(),
            clientSecret = null,
            clientSecretExpiresAt = this.clientSecretExpiresAt,
            clientName = this.clientName ?: "",
            clientAuthenticationMethods = this.clientAuthenticationMethods
                ?.split(",")?.map { it.trim() } ?: emptyList(),
            authorizationGrantTypes = this.authorizationGrantTypes
                ?.split(",")?.map { it.trim() } ?: emptyList(),
            redirectUris = this.redirectUris
                ?.split(",")?.map { it.trim() } ?: emptyList(),
            postLogoutRedirectUris = this.postLogOutRedirectUris
                ?.split(",")?.map { it.trim() } ?: emptyList(),
            scopes = this.scope
                ?.split(",")?.map { it.trim() } ?: emptyList(),
            clientSettings = cs.toDTO(),
            tokenSettings = ts.toDTO()
        )
    }
}