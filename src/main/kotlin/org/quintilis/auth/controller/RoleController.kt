package org.quintilis.auth.controller

import java.util.UUID
import org.quintilis.auth.service.RoleService
import org.quintilis.common.dto.auth.PermissionDTO
import org.quintilis.common.dto.auth.RoleDTO
import org.quintilis.common.dto.auth.UserDTO
import org.quintilis.common.exception.NotFoundException
import org.quintilis.common.response.ApiResponse
import org.quintilis.common.response.PageResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class RoleController(private val roleService: RoleService) {

    public data class RoleUpdate(
        val id: Int,
        val name: String,
        val displayName: String,
        val color: String,
        val icon: String?,
        val priority: Int,
        val permissionIds: List<Int>
    )

    data class RoleNew(
        val name: String,
        val displayName: String,
        val color: String,
        val permissionIds: List<Int>,
        val priority: Int
    )

    @PostMapping("/role/new")
    @PreAuthorize("hasRole('ADMIN')")
    fun newRole(
        @RequestBody roleDTO: RoleNew,
    ) : ApiResponse<RoleDTO> {
        return ApiResponse.success(roleService.create(roleDTO))
    }

    @GetMapping("/roles")
    fun getAllRoles(): ApiResponse<List<RoleDTO>> {
        return ApiResponse.success(roleService.getAllRoles())
    }

    @GetMapping("/roles/{id}")
    fun getRoleById(@PathVariable id: Int): ApiResponse<RoleDTO> {
        val role = roleService.getRoleById(id) ?: throw NotFoundException("Role not found")
        return  ApiResponse.success(role)
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permissions/all")
    fun getAllPermissions(): ApiResponse<List<PermissionDTO>> {
        return ApiResponse.success(roleService.getAllPermission())
    }

    @GetMapping("/permissions")
    fun getAllPermissionsPaged(
        @RequestParam("page") pageParam: Int?
    ): ApiResponse<PageResponse<PermissionDTO>> {
        val page = pageParam ?: 1
        return  ApiResponse.success(roleService.getAllPermissions(page))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/roles/{id}")
    fun updateRole(
        @PathVariable id: Int,
        @RequestBody role: RoleUpdate
    ): ApiResponse<RoleDTO>{
        return ApiResponse.success(roleService.updateRole(id, role))
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/roles/{id}/permissions")
    fun updateRolePermissions(
            @PathVariable id: Int,
            @RequestBody permissionIds: List<Int>
    ): ApiResponse<RoleDTO> {
        val updated =
                roleService.updateRolePermissions(id, permissionIds)
                        ?: throw NotFoundException("Role not found")
        return ApiResponse.success(updated)
    }

    @GetMapping("/users")
    fun getAllUsers(): ApiResponse<List<UserDTO>> {
        return ApiResponse.success(roleService.getAllUsers())
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: UUID): ApiResponse<UserDTO> {
        val user = roleService.getUserById(id) ?: throw NotFoundException("User not found")
        return ApiResponse.success(user)
    }

    @PutMapping("/users/{id}/roles")
    fun updateUserRoles(
            @PathVariable id: UUID,
            @RequestBody roleIds: List<Int>
    ): ApiResponse<UserDTO> {
        val updated =
                roleService.updateUserRoles(id, roleIds) ?: throw NotFoundException("User not found")
        return ApiResponse.success(updated)
    }
}
