package org.quintilis.auth.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SpaController {

    // Revertendo para a versão mais simples que captura tudo exceto rotas de API e estáticos conhecidos
    // Isso fará com que requisições para .map ou hooks retornem o index.html (200 OK) em vez de 404,
    // o que evita que o erro seja exibido ou tratado como falha crítica pelo navegador/frontend.
    @RequestMapping(value = ["/{path:^(?!api|oauth2|assets|static|favicon.ico|index.html).*}", "/"])
    fun forward(): String {
        return "forward:/index.html"
    }
}
