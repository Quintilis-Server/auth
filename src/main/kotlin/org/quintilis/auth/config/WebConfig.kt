package org.quintilis.auth.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve arquivos estáticos da pasta /static/ no classpath
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }
}
