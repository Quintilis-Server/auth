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
        val savedRequest =
                request.session?.getAttribute("SPRING_SECURITY_SAVED_REQUEST") as?
                        org.springframework.security.web.savedrequest.DefaultSavedRequest
        val savedRedirectUrl = savedRequest?.redirectUrl

        if (savedRedirectUrl != null && savedRedirectUrl.contains("/oauth2/authorize")) {
            request.session?.removeAttribute("SPRING_SECURITY_SAVED_REQUEST")
            clearAuthenticationAttributes(request)
            redirectStrategy.sendRedirect(request, response, savedRedirectUrl)
            return
        }

        val email =
                if (authentication is OAuth2AuthenticationToken) {
                    authentication.principal?.attributes["email"]?.toString()
                } else {
                    authentication.name
                }

        if (email == null) {
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }

        val userDetails = userDetailsService.loadUserByUsername(email)
        val token = jwtService.generateToken(userDetails)

        val redirectUrl =
                UriComponentsBuilder.fromUriString(frontendUrl)
                        .path("/oauth2/callback")
                        .queryParam("token", token)
                        .build()
                        .toUriString()

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
