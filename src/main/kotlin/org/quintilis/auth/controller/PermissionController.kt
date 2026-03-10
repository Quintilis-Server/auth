package org.quintilis.auth.controller

import org.quintilis.auth.service.PermissionService
import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.PermissionDTO
import org.quintilis.common.entities.auth.Permission
import org.quintilis.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val permissionService: PermissionService
): BaseController<Permission, Int, PermissionDTO, PermissionService.NewPermission>(permissionService) {

    override val allowCreate: Boolean
        get() = true
    @GetMapping("/list")
    fun getAllPermissionsList(): ApiResponse<List<PermissionDTO>> {
        return ApiResponse.success(permissionService.getAllPermissionsList())
    }
}