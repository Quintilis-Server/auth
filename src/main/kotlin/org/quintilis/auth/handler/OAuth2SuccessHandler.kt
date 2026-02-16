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
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val jwtService: JWTService,
    private val userDetailsService: UserDetailsService
) : SavedRequestAwareAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

    @Value("\${frontend.url:http://localhost:3000}")
    private lateinit var frontendUrl: String

    private val requestCache = HttpSessionRequestCache()

    init {
        setTargetUrlParameter("targetUrl")
        defaultTargetUrl = "/"
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val savedRequest = requestCache.getRequest(request, response)

//        logger.info("Authentication Success. SavedRequest: {}", savedRequest?.redirectUrl)

        if (savedRequest != null) {
//            logger.info("Redirecionando para a requisição salva: {}", savedRequest.redirectUrl)
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }

//        logger.info("Nenhuma requisição salva encontrada. Iniciando fluxo de login direto.")

        val email = if (authentication is OAuth2AuthenticationToken) {
            authentication.principal?.attributes["email"]?.toString()
        } else {
            authentication.name
        }

        if (email == null) {
//            logger.error("Email não encontrado na autenticação.")
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }

        val userDetails = userDetailsService.loadUserByUsername(email)
        val token = jwtService.generateToken(userDetails)

        val redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path("/oauth2/callback")
            .queryParam("token", token)
            .build().toUriString()

//        logger.info("Redirecionando para o frontend com token: {}", redirectUrl)

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
