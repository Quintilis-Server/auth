package org.quintilis.auth.controller

import org.quintilis.auth.service.JWTService
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.quintilis.common.response.ApiResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JWTService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    data class LoginRequest(val login: String, val password: String)
    data class LoginResponse(val token: String)

    data class RegisterRequest(val username: String, val email: String, val password: String)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ApiResponse<String> {
        // 1. Cria o usuário vazando a senha no BCrypt
        val newUser = User().apply {
            this.username = request.username
            this.email = request.email
            this.passwordHash = passwordEncoder.encode(request.password) // A MÁGICA AQUI
            this.role = "USER"
        }

        // 2. Salva no banco de dados
        userRepository.save(newUser)

        return ApiResponse.success("Usuário registrado com sucesso!")
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ApiResponse<LoginResponse>{
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.login, request.password)
        )

        val userId = authentication.name;

        val token = jwtService.generateToken(userId)

        return ApiResponse.success(LoginResponse(token))
    }
}