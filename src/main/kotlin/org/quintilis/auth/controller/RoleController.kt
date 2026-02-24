package org.quintilis.auth.controller

import java.util.UUID
import org.quintilis.auth.service.RoleService
import org.quintilis.common.dto.auth.PermissionDTO
import org.quintilis.common.dto.auth.RoleDTO
import org.quintilis.common.dto.auth.UserDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class RoleController(private val roleService: RoleService) {

    @GetMapping("/roles")
    fun getAllRoles(): ResponseEntity<List<RoleDTO>> {
        return ResponseEntity.ok(roleService.getAllRoles())
    }

    @GetMapping("/roles/{id}")
    fun getRoleById(@PathVariable id: Int): ResponseEntity<RoleDTO> {
        val role = roleService.getRoleById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(role)
    }

    @GetMapping("/permissions")
    fun getAllPermissions(): ResponseEntity<List<PermissionDTO>> {
        return ResponseEntity.ok(roleService.getAllPermissions())
    }

    @PutMapping("/roles/{id}/permissions")
    fun updateRolePermissions(
            @PathVariable id: Int,
            @RequestBody permissionIds: List<Int>
    ): ResponseEntity<RoleDTO> {
        val updated =
                roleService.updateRolePermissions(id, permissionIds)
                        ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<UserDTO>> {
        return ResponseEntity.ok(roleService.getAllUsers())
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserDTO> {
        val user = roleService.getUserById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{id}/roles")
    fun updateUserRoles(
            @PathVariable id: UUID,
            @RequestBody roleIds: List<Int>
    ): ResponseEntity<UserDTO> {
        val updated =
                roleService.updateUserRoles(id, roleIds) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(updated)
    }
}
