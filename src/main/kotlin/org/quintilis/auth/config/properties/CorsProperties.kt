package org.quintilis.auth.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "quintilis.cors")
class CorsProperties {
    var allowedOrigins: List<String> = ArrayList()
}