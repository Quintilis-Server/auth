package org.quintilis.auth.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaController {

    // Adicionei 'index.html' na regex de exclusão para evitar o loop infinito
    // Agora ele captura tudo EXCETO: api, oauth2, assets, static, favicon.ico E index.html
    @RequestMapping(value = ["/{path:^(?!api|oauth2|assets|static|favicon.ico|index.html).*}", "/"])
    fun forward(): String {
        return "forward:/index.html"
    }
}
