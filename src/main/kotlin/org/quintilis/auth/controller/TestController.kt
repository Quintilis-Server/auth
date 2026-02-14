package org.quintilis.auth.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/api/me")
    fun me(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        return mapOf(
            "message" to "Você acessou uma rota protegida com OAuth2!",
            "user_id" to jwt.subject,
            "claims" to jwt.claims
        )
    }
}
