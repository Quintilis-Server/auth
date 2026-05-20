package org.quintilis.auth.controller

import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.FrontendRouteDTO
import org.quintilis.common.entities.auth.FrontendRoute
import org.quintilis.common.service.FrontendRouteService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/frontend-routes")
class FrontendRouteController(private val frontendRouteService: FrontendRouteService) : BaseController<FrontendRoute, UUID, FrontendRouteDTO, FrontendRouteDTO>(frontendRouteService) {
    // CRUD inherits from BaseController
}
