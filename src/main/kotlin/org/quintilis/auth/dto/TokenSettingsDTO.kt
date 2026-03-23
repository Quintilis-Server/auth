package org.quintilis.auth.dto

import java.io.Serializable

data class TokenSettingsDTO(
    val reuseRefreshTokens: Boolean = true,
    val accessTokenTtlHours: Long = 1,
    val refreshTokenTtlHours: Long = 720,
    val authorizationCodeTtlMinutes: Long = 5
): Serializable