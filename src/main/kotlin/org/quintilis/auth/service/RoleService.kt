package org.quintilis.auth.service

import org.quintilis.auth.controller.RoleNew
import java.util.UUID
import org.quintilis.common.dto.auth.RoleDTO
import org.quintilis.common.dto.auth.UserDTO
import org.quintilis.common.entities.auth.Role
import org.quintilis.common.repositories.auth.PermissionRepository
import org.quintilis.common.repositories.auth.RoleRepository
import org.quintilis.common.repositories.auth.UserRepository
import org.quintilis.common.service.BaseService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KProperty1

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository
): BaseService<Role, Int, RoleDTO, RoleNew>(roleRepository) {

    // ========================================================================
    // 1. CONTRATOS DO BASE SERVICE (A MÁGICA DA CONVERSÃO E BUSCA)
    // ========================================================================

    override fun getSearchFields(): List<KProperty1<Role, *>> {
        return listOf(
            Role::name,
            Role::priority,
            Role::displayName
        )
    }

    override fun newDTOToEntity(newDTO: RoleNew): Role {
        return Role().apply {
            this.name = newDTO.name
            this.displayName = newDTO.displayName
            this.color = newDTO.color
            this.priority = newDTO.priority
            this.icon = newDTO.icon
            this.permissions.clear()
            this.permissions.addAll(permissionRepository.findAllById(newDTO.permissionIds))
        }
    }

    override fun updateEntityFromDTO(dto: RoleDTO, entity: Role) {
        entity.name = dto.name
        entity.displayName = dto.displayName
        entity.color = dto.color
        entity.priority = dto.priority
        entity.icon = dto.icon
        entity.permissions.clear()
        entity.permissions.addAll(permissionRepository.findAllById(dto.permissions.mapNotNull { it.id }))
    }

    @Transactional
    @CacheEvict("roles", "users", allEntries = true)
    override fun create(dto: RoleNew): RoleDTO {
        // Usa a lógica pronta do BaseService e só adiciona o CacheEvict por cima!
        return super.create(dto)
    }

    @Transactional
    @CacheEvict("roles", "users", allEntries = true)
    override fun update(dto: RoleDTO, id: Int): RoleDTO {
        return super.update(dto, id)
    }

    @Transactional
    @CacheEvict("roles", "users", allEntries = true)
    override fun delete(id: Int, hardDelete: Boolean): RoleDTO {
        return super.delete(id, hardDelete)
    }

    // ========================================================================
    // 3. MÉTODOS CUSTOMIZADOS (LISTAS ESPECÍFICAS E RELACIONAMENTOS)
    // ========================================================================

    // O Controller deve usar o findById do BaseService, mas você manteve essa lista
    // customizada aqui. Se você precisar da lista inteira sem paginação:
    @Cacheable("roles")
    fun getAllRoles(): List<RoleDTO> {
        return roleRepository.findAll().sortedByDescending { it.priority ?: 0 }.map { it.toDTO() }
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

    @Transactional
    @CacheEvict("users", allEntries = true)
    fun updateUserRoles(userId: UUID, roleIds: List<Int>): UserDTO? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val roles = roleRepository.findAllById(roleIds)
        user.roles.clear()
        user.roles.addAll(roles)
        return userRepository.save(user).toDTO()
    }

    @Cacheable("users")
    fun getAllUsers(): List<UserDTO> {
        return userRepository.findAll().map { it.toDTO() }
    }

    fun getUserById(id: UUID): UserDTO? {
        return userRepository.findById(id).orElse(null)?.toDTO()
    }

    @Cacheable("role", key = "#roleName")
    fun getRoleByName(roleName: String): RoleDTO? {
        return roleRepository.findByName(roleName)?.toDTO()
    }
}