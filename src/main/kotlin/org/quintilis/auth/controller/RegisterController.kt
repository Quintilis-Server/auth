package org.quintilis.auth.controller

import java.util.UUID
import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.RoleRepository
import org.quintilis.common.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class RegisterController(
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val passwordEncoder: PasswordEncoder
) {

    @GetMapping("/register")
    fun showRegisterForm(): String {
        return "register"
    }

    @PostMapping("/register")
    fun register(
            @RequestParam username: String,
            @RequestParam email: String,
            @RequestParam password: String,
            model: Model
    ): String {
        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Usuário já existe.")
            return "register"
        }
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "Email já cadastrado.")
            return "register"
        }

        val defaultRole = roleRepository.findByName("USER")

        val newUser =
                User().apply {
                    this.id = UUID.randomUUID()
                    this.username = username
                    this.email = email
                    this.passwordHash = passwordEncoder.encode(password)
                    if (defaultRole != null) {
                        this.roles.add(defaultRole)
                    }
                }

        userRepository.save(newUser)

        return "redirect:/login?success"
    }
}
