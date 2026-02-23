package org.quintilis.auth.service

import org.quintilis.common.entities.auth.User
import org.quintilis.common.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

        override fun loadUserByUsername(username: String): UserDetails {
                val user: User =
                        userRepository.findByUsername(username)
                                ?: userRepository.findByEmail(username)
                                        ?: throw UsernameNotFoundException(
                                        "Usuário não encontrado: $username"
                                )

                return org.springframework.security.core.userdetails.User.builder()
                        .username(
                                user.id.toString()
                        ) // Usamos o ID como "username" interno do Spring Security
                        .password(user.passwordHash)
                        .roles(user.role!!)
                        .build()
        }
}
