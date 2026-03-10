package org.quintilis.auth.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController // 🔥 Importante: mudou de @Controller para @RestController
class FrontendController(
    // 🔥 Pega o arquivo físico diretamente do classpath!
    @Value("classpath:/static/index.html")
    private val indexHtml: Resource
) {

    // Avisa que a resposta vai ser um HTML
    @GetMapping(
        value = ["/", "/login", "/forgot-password", "/reset-password"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun serveReactFrontend(): Resource {
        // Devolve o arquivo puro, sem intermediários.
        return indexHtml
    }
}