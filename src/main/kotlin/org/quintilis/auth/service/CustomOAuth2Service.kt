package org.quintilis.auth.service

import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2Service(
    private val userRepository: UserRepository
): DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User? {
        val oauthUser = super.loadUser(userRequest)
        val provider = userRequest?.clientRegistration?.registrationId

        val attributes = oauthUser.attributes
        val email = attributes["email"]?.toString() ?: ""
        val name = attributes["name"]?.toString() ?: ""

        val providerId = attributes["sub"]?.toString() ?: attributes["id"]?.toString() ?: ""

        var user = if(provider == "google") {
            userRepository.findByGoogleId(providerId)
        } else {
            userRepository.findByMicrosoftId(providerId)
        }

        if(user == null && email.isNotEmpty()){
            user = userRepository.findByEmail(email)
            if(user != null){
                if(provider == "google") user.googleId = providerId
                if(provider == "microsoft") user.microsoftId = providerId
                userRepository.save(user)
            }
        }

        if(user == null){
            user = User().apply {
                this.username = name.replace(" ", "_").lowercase() + "_" + (1000..9999).random()
                this.email = email
                this.role = "USER"

                if (provider == "google") {
                    this.googleId = providerId
                }
                if (provider == "microsoft") {
                    this.microsoftId = providerId
                }
            }
            user = userRepository.save(user)
        }
        return DefaultOAuth2User(
            oauthUser.authorities,
            attributes.plus("internal_user_id" to user.id.toString()),
            "email"
        )
    }
}