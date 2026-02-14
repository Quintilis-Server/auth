package org.quintilis.auth.dto

data class LoginRequest(
    val username: String,
    val password: String
)