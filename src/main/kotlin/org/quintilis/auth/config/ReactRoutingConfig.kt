package org.quintilis.auth.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ReactRoutingConfig : WebMvcConfigurer {

    // 1. Garante que o Spring saiba onde achar os arquivos estáticos do React
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }

    // 2. Qualquer rota que não seja /api ou /oauth2 vai cair no index.html do React!
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{spring:\\w+}")
            .setViewName("forward:/index.html")
        registry.addViewController("/**/{spring:\\w+}")
            .setViewName("forward:/index.html")
    }
}