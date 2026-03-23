package org.quintilis.auth.dto

import java.io.Serializable

data class ClientSettingsDTO(
    val requireProofKey: Boolean = false,
    val requireAuthorizationConsent: Boolean = false
): Serializable