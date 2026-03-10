package org.quintilis.auth.controller

import org.quintilis.auth.service.RoleService
import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.RoleDTO
import org.quintilis.common.entities.auth.Role
import org.quintilis.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RoleNew(
    val name: String,
    val displayName: String,
    val color: String,
    val permissionIds: List<Int>,
    val priority: Int,
    val icon: String?
)

@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) : BaseController<Role, Int, RoleDTO, RoleNew>(roleService) {
    @GetMapping("/list")
    fun getAllRoles(): ApiResponse<List<RoleDTO>> {
        return ApiResponse.success(roleService.getAllRoles())
    }
}