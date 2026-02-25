package org.quintilis.auth.service

import java.util.UUID
import org.quintilis.common.dto.auth.PermissionDTO
import org.quintilis.common.dto.auth.RoleDTO
import org.quintilis.common.dto.auth.UserDTO
import org.quintilis.common.repositories.PermissionRepository
import org.quintilis.common.repositories.RoleRepository
import org.quintilis.common.repositories.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
        private val roleRepository: RoleRepository,
        private val permissionRepository: PermissionRepository,
        private val userRepository: UserRepository
) {

    @Cacheable("roles")
    fun getAllRoles(): List<RoleDTO> {
        return roleRepository.findAll().sortedByDescending { it.priority ?: 0 }.map { it.toDTO() }
    }

    fun getRoleById(id: Int): RoleDTO? {
        return roleRepository.findById(id).orElse(null)?.toDTO()
    }

    @Cacheable("permissions")
    fun getAllPermissions(): List<PermissionDTO> {
        return permissionRepository.findAll().map { it.toDTO() }
    }

    @Transactional
    @CacheEvict("roles", allEntries = true)
    fun updateRolePermissions(roleId: Int, permissionIds: List<Int>): RoleDTO? {
        val role = roleRepository.findById(roleId).orElse(null) ?: return null
        val permissions = permissionRepository.findAllById(permissionIds)
        role.permissions.clear()
        role.permissions.addAll(permissions)
        return roleRepository.save(role).toDTO()
    }

    @Cacheable("users")
    fun getAllUsers(): List<UserDTO> {
        return userRepository.findAll().map { it.toDTO() }
    }

    fun getUserById(id: UUID): UserDTO? {
        return userRepository.findById(id).orElse(null)?.toDTO()
    }

    @Transactional
    @CacheEvict("users", allEntries = true)
    fun updateUserRoles(userId: UUID, roleIds: List<Int>): UserDTO? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val roles = roleRepository.findAllById(roleIds)
        user.roles.clear()
        user.roles.addAll(roles)
        return userRepository.save(user).toDTO()
    }
}
