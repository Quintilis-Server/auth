package org.quintilis.auth.service

import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.RoleRepository
import org.quintilis.common.repositories.UserRepository
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOidcUserService(
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository
) : OidcUserService() {

    @Transactional
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId

        val attributes = oidcUser.attributes
        val email = attributes["email"]?.toString() ?: ""
        val name = attributes["name"]?.toString() ?: "Usuario"
        val providerId = attributes["sub"]?.toString() ?: ""

        var user =
                if (provider == "google") {
                    userRepository.findByGoogleId(providerId)
                } else {
                    userRepository.findByMicrosoftId(providerId)
                }

        if (user != null) {
            return injectInternalId(oidcUser, user.id.toString())
        }

        if (email.isNotEmpty()) {
            user = userRepository.findByEmail(email)
            if (user != null) {
                var changed = false
                if (provider == "google" && user.googleId != providerId) {
                    user.googleId = providerId
                    changed = true
                }
                if (provider == "microsoft" && user.microsoftId != providerId) {
                    user.microsoftId = providerId
                    changed = true
                }

                if (changed) {
                    userRepository.saveAndFlush(user)
                }
            }
        }

        if (user == null) {
            val newUser =
                    User().apply {
                        var desiredUsername = name
                        if (userRepository.findByUsername(desiredUsername) != null) {
                            desiredUsername = name + "_" + (1000..9999).random()
                        }
                        this.username = desiredUsername
                        this.email = email
                        val defaultRole = roleRepository.findByName("USER")
                        if (defaultRole != null) {
                            this.roles.add(defaultRole)
                        }
                        if (provider == "google") this.googleId = providerId
                        if (provider == "microsoft") this.microsoftId = providerId
                    }
            user = userRepository.saveAndFlush(newUser)
        }

        return injectInternalId(oidcUser, user!!.id.toString())
    }


    private fun injectInternalId(oidcUser: OidcUser, internalId: String): OidcUser {
        // Adiciona o UUID no token
        val newIdTokenClaims = oidcUser.idToken.claims.toMutableMap()
        newIdTokenClaims["internal_id"] = internalId
        val newIdToken = OidcIdToken(
            oidcUser.idToken.tokenValue,
            oidcUser.idToken.issuedAt,
            oidcUser.idToken.expiresAt,
            newIdTokenClaims
        )

        // Adiciona o UUID no UserInfo (se o Google tiver mandado um)
        val newUserInfo = oidcUser.userInfo?.let {
            val userInfoClaims = it.claims.toMutableMap()
            userInfoClaims["internal_id"] = internalId
            OidcUserInfo(userInfoClaims)
        }

        // Retorna a classe padrão do Spring apontando para a nossa nova chave!
        return DefaultOidcUser(oidcUser.authorities, newIdToken, newUserInfo, "internal_id")
    }
}
