package org.quintilis.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.service.JWTService
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

    @Value("\${frontend.url:http://localhost:3000}")
    private lateinit var frontendUrl: String

    private val requestCache = HttpSessionRequestCache()

    init {
        // Define uma URL padrão para o caso de login direto (sem requisição salva)
        setTargetUrlParameter("targetUrl")
        defaultTargetUrl = "/"
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val savedRequest = requestCache.getRequest(request, response)

        // Se existe uma requisição salva, significa que o login foi iniciado por um fluxo
        // como o /oauth2/authorize. Deixamos o Spring continuar o fluxo padrão.
        if (savedRequest != null) {
            super.onAuthenticationSuccess(request, response, authentication)
            return
        }

        // Se NÃO existe requisição salva, é um login direto.
        // Neste caso, geramos nosso token JWT e redirecionamos para o nosso frontend.
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

        // Redireciona para o frontend com o token
        val redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path("/oauth2/callback")
            .queryParam("token", token)
            .build().toUriString()

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
