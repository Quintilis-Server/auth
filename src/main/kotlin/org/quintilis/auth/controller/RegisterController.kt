package org.quintilis.auth.controller

import org.quintilis.auth.service.RoleService
import java.util.UUID
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.RoleRepository
import org.quintilis.common.repositories.UserRepository
import org.quintilis.common.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class RegisterController(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val roleService: RoleService
) {

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
