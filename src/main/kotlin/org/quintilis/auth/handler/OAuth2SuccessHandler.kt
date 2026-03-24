package org.quintilis.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
        private val jwtService: JWTService,
        private val userDetailsService: UserDetailsService
) : SavedRequestAwareAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

    @Value("\${frontend.url:http://localhost:3000}") private lateinit var frontendUrl: String

    init {
        setTargetUrlParameter("targetUrl")
        defaultTargetUrl = "/"
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val savedRequest = request.session?.getAttribute("SPRING_SECURITY_SAVED_REQUEST") as? DefaultSavedRequest
        val savedRedirectUrl = savedRequest?.redirectUrl

        logger.info("=== AUTH SUCCESS ===")
        logger.info("Authentication type: ${authentication::class.simpleName}")
        logger.info("Saved request: $savedRedirectUrl")

        if (savedRedirectUrl != null) {
            // Login direto sem fluxo OAuth2 — redireciona para o frontend
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }

        // Fluxo direto (sem OAuth2 flow) — só para login sem frontend OAuth2
        if (authentication is OAuth2AuthenticationToken) {
            // Login social sem fluxo OAuth2 — redireciona para o frontend com token JWT
            val email = authentication.principal?.attributes["email"]?.toString()
            if (email != null) {
                val userDetails = userDetailsService.loadUserByUsername(email)
                val token = jwtService.generateToken(userDetails)
                val redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/oauth2/callback")
                    .queryParam("token", token)
                    .build()
                    .toUriString()
                clearAuthenticationAttributes(request)
                redirectStrategy.sendRedirect(request, response, redirectUrl)
                return
            }
        }

        // Fallback
        redirectStrategy.sendRedirect(request, response, frontendUrl)
        return
    }
}
