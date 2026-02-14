package org.quintilis.auth.service

import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOidcUserService(
    private val userRepository: UserRepository
) : OidcUserService() {

    @Transactional
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        
        val attributes = oidcUser.attributes
        val email = attributes["email"]?.toString() ?: ""
        val name = attributes["name"]?.toString() ?: "Usuario"
        val providerId = attributes["sub"]?.toString() ?: ""

        var user = if(provider == "google") {
            userRepository.findByGoogleId(providerId)
        } else {
            userRepository.findByMicrosoftId(providerId)
        }

        if (user != null) {
            return DefaultOidcUser(
                oidcUser.authorities,
                oidcUser.idToken,
                oidcUser.userInfo,
                "email"
            )
        }

        if(email.isNotEmpty()){
            user = userRepository.findByEmail(email)
            if(user != null){
                var changed = false
                if(provider == "google" && user.googleId != providerId) {
                    user.googleId = providerId
                    changed = true
                }
                if(provider == "microsoft" && user.microsoftId != providerId) {
                    user.microsoftId = providerId
                    changed = true
                }
                
                if (changed) {
                    userRepository.saveAndFlush(user)
                }
            }
        }

        if(user == null){
            val newUser = User().apply {
                // ID removido: Deixa o Hibernate/Banco gerar
                this.username = name.replace(" ", "_").lowercase() + "_" + (1000..9999).random()
                this.email = email
                this.role = "USER"
//                this.isVerified = true
                if (provider == "google") this.googleId = providerId
                if (provider == "microsoft") this.microsoftId = providerId
            }
            user = userRepository.saveAndFlush(newUser)
        }

        return DefaultOidcUser(
            oidcUser.authorities,
            oidcUser.idToken,
            oidcUser.userInfo,
            "email"
        )
    }
}
