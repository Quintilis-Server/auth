package org.quintilis.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = [
    "org.quintilis.common", // Para utilitários e DTOs
    "org.quintilis.auth"
])
@EntityScan(basePackages = [
    "org.quintilis.common.entities",
    "org.quintilis.auth.entities" // Apenas entidades do Fórum (incluindo o User simplificado)
])
@EnableJpaRepositories(basePackages = ["org.quintilis.common.repositories", "org.quintilis.auth.repositories"])
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
