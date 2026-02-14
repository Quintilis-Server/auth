package org.quintilis.auth.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaController {

    // Redireciona rotas do frontend para o index.html do React
    // Exclui /api, /oauth2, /login (POST), /register (POST) e arquivos estáticos
    @RequestMapping(value = ["/{path:[^\\.]*}", "/login", "/register"])
    fun redirect(): String {
        return "forward:/index.html"
    }
}
