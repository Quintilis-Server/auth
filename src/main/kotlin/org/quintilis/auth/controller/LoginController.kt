package org.quintilis.auth.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.dto.LoginRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class LoginController(
    private val authenticationManager: AuthenticationManager,
) {
    private val securityContextHolderStrategy: SecurityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy()
    private val securityContextRepository: SecurityContextRepository =
        HttpSessionSecurityContextRepository()

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        // 1. Cria o token não autenticado com os dados recebidos
        val token =
            UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username,
                loginRequest.password
            )

        // 2. Autentica usando o seu CustomUserDetailsService (já configurado no Spring)
        val authentication = authenticationManager.authenticate(token)

        // 3. Salva a autenticação na Sessão (Cria o JSESSIONID)
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authentication
        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
    }
}