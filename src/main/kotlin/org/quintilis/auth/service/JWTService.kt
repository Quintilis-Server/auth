package org.quintilis.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JWTService {
    @Value("\${jwt.secret:uma-chave-secreta-muito-longa-para-o-projeto-quintilis}")
    private lateinit var secretString: String

    @Value("\${jwt.expiration:640000}")
    private val expirationTime: Long = 0L

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretString.toByteArray())
    }

    fun generateToken(userId: String): String {
        return Jwts.builder()
            .subject(userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey)
            .compact()
    }

    fun extractUserId(token: String): String {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }
}