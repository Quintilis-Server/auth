package org.quintilis.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.service.JWTService
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

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // Pega a URL de redirect que o frontend pediu (e que o Spring validou)
        val targetUrl = determineTargetUrl(request, response, authentication)
        
        val email = if (authentication is OAuth2AuthenticationToken) {
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

        // Adiciona o token na URL de redirect
        val redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("token", token)
            .build().toUriString()

        // Limpa a sessão e redireciona
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
