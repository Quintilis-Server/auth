package org.quintilis.auth.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

// Removido @Configuration para evitar a criação de um bean duplicado.
// @EnableConfigurationProperties na classe que a usa é o suficiente.
@ConfigurationProperties(prefix = "quintilis.oauth2")
open class ClientSettingsProperties {
    lateinit var clients: List<Client>

    class Client {
        lateinit var clientId: String
        lateinit var clientSecret: String
        lateinit var redirectUris: Set<String>
        lateinit var scopes: Set<String>
    }
}
