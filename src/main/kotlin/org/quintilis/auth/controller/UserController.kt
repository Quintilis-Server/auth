package org.quintilis.auth.controller

import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.UserDTO
import org.quintilis.common.entities.auth.User
import org.quintilis.common.exception.InternalServerException
import org.quintilis.common.exception.UnauthorizedException
import org.quintilis.common.response.ApiResponse
import org.quintilis.common.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
): BaseController<User, UUID, UserDTO, UserDTO>(userService){
    override val allowCreate: Boolean
        get() = false

    override fun update(id: UUID, dto: UserDTO): ApiResponse<UserDTO> {
        val authentication = SecurityContextHolder.getContext().authentication

        val loggedInUserId = getAuthenticatedUserId()

        val isOwner = (id == loggedInUserId)
        val isAdmin = authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } ?: false// Ajuste o nome da role se necessário

        if (!isOwner && !isAdmin) {
            throw UnauthorizedException("Você não tem permissão para alterar este usuário.")
        }

        // 1. Pega os dados originais do banco
        val existingUser = userService.findById(id).orElseThrow { UnauthorizedException("User with id $id not found.") }
        val existingDto = existingUser.toDTO()

        // 2. Cria uma cópia cega, trocando SÓ o que a pessoa tem permissão
        val sanitizedDto = existingDto.copy(
            username = if (isOwner) dto.username else existingDto.username,
            avatarPath = if (isOwner) dto.avatarPath else existingDto.avatarPath,
            roles = if (isAdmin) dto.roles else existingDto.roles
        )

        // 3. Manda o DTO limpo para o BaseController fazer o save
        return super.update(id, sanitizedDto)
    }

    override fun delete(@PathVariable id: UUID): ApiResponse<UserDTO> {
        val loggedInUserId = getAuthenticatedUserId()

        if (id != loggedInUserId) {
            throw UnauthorizedException("Acesso negado. Você só pode deletar sua própria conta.")
        }

        return super.delete(id)
    }

    @GetMapping("/me")
    fun getCurrentUser(): ApiResponse<UserDTO> {
        val loggedInUserId = getAuthenticatedUserId()
        val user = userService.findById(loggedInUserId)
            .orElseThrow { InternalServerException("User Not Found") }.toDTO()
        return ApiResponse.success(user)
    }
}