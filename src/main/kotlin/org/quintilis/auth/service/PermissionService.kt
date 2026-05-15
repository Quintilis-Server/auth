package org.quintilis.auth.service

import org.quintilis.common.dto.auth.PermissionDTO
import org.quintilis.common.entities.auth.Permission
import org.quintilis.common.repositories.auth.PermissionRepository
import org.quintilis.common.service.BaseService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.reflect.KProperty1

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository
): BaseService<Permission, Int, PermissionDTO, PermissionService.NewPermission>(permissionRepository) {
    override fun newDTOToEntity(newDTO: NewPermission): Permission {
        return Permission().apply {
            this.name = newDTO.name
        }
    }

    override fun getSearchFields(): List<KProperty1<Permission, *>> {
        return listOf(
            Permission::name
        )
    }

    override fun updateEntityFromDTO(
        dto: PermissionDTO,
        entity: Permission
    ) {
        entity.name = dto.name
    }

    @Cacheable("all_permissions")
    fun getAllPermissionsList(): List<PermissionDTO> {
        return permissionRepository.findByIsActive().map { it.toDTO() }
    }

    @Cacheable("permission", key = "id")
    override fun findById(id: Int, includeInactive: Boolean): Permission {
        return super.findById(id, includeInactive)
    }

    @CacheEvict("permission", "all_permissions", allEntries = true)
    override fun create(dto: NewPermission): PermissionDTO {
        return super.create(dto)
    }


    data class NewPermission(
        val name: String
    )
}