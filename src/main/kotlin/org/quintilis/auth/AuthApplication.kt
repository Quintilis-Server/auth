package org.quintilis.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(basePackages = [
    "org.quintilis"
])
@EntityScan(basePackages = [
    "org.quintilis"
])
@EnableJpaRepositories(basePackages = ["org.quintilis.common.repositories"])
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
