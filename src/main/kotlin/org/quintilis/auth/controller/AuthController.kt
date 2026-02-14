package org.quintilis.auth.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.quintilis.auth.dto.LoginRequest
import org.quintilis.auth.dto.RegisterRequest
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val securityContextHolderStrategy: SecurityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
    private val securityContextRepository: SecurityContextRepository = HttpSessionSecurityContextRepository()

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest, request: HttpServletRequest, response: HttpServletResponse) {
        // 1. Cria o token não autenticado com os dados recebidos
        val token = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.username, loginRequest.password)
        
        // 2. Autentica usando o seu CustomUserDetailsService (já configurado no Spring)
        val authentication = authenticationManager.authenticate(token)

        // 3. Salva a autenticação na Sessão (Cria o JSESSIONID)
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authentication
        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<String> {
        if (userRepository.findByUsername(request.username) != null || userRepository.findByEmail(request.email) != null) {
            return ResponseEntity.badRequest().body("Usuário ou Email já existem.")
        }

        val newUser = User().apply {
            id = UUID.randomUUID()
            username = request.username
            email = request.email
            passwordHash = passwordEncoder.encode(request.password)
            role = "USER"
        }

        userRepository.save(newUser)
        return ResponseEntity.ok("Usuário criado com sucesso!")
    }
}