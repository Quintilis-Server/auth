package org.quintilis.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.service.JWTService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val jwtService: JWTService
): SimpleUrlAuthenticationSuccessHandler() {
    @Value("\${frontend.url:http://localhost:3000}")
    private lateinit var frontendUrl: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User


        val internalUserId = oauth2User.attributes["internal_user_id"]?.toString()
            ?: throw IllegalStateException("ID interno não encontrado após login social")

        val token = jwtService.generateToken(internalUserId)

        val targetUrl ="$frontendUrl/oauth2/callback?token=$token"
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
