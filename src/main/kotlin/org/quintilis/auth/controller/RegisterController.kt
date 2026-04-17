package org.quintilis.auth.controller

import org.quintilis.auth.service.RoleService
import org.quintilis.common.entities.auth.User
import org.quintilis.common.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class RegisterController(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val roleService: RoleService,
    @Value("classpath:/static/index.html") private val indexHtml: Resource
) {

    @GetMapping("/register")
    @ResponseBody // Importante: para retornar o arquivo direto, não uma View do Thymeleaf
    fun serveRegisterPage(): Resource {
        return indexHtml
    }

    @PostMapping("/register")
    fun register(
            @RequestParam username: String,
            @RequestParam email: String,
            @RequestParam password: String,
            model: Model
    ): String {
        if (userService.findByUsername(username) != null) {
            model.addAttribute("error", "Usuário já existe.")
            return "register"
        }
        if (userService.findByEmail(email) != null) {
            model.addAttribute("error", "Email já cadastrado.")
            return "register"
        }

        var defaultRole = roleService.getRoleByName("USER")
        if(defaultRole == null) {
            defaultRole = roleService.getRoleByName("GUEST")
        }

        val newUser =
                User().apply {
                    this.username = username
                    this.email = email
                    this.passwordHash = passwordEncoder.encode(password)
                    if (defaultRole != null) {
                        this.roles.add(defaultRole.toEntity())
                    }
                }

        userService.save(newUser)

        return "redirect:/login?success"
    }
}
