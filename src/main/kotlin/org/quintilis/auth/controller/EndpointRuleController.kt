package org.quintilis.auth.controller

import jakarta.websocket.server.PathParam
import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.EndpointRuleDTO
import org.quintilis.common.entities.auth.EndpointRule
import org.quintilis.common.response.ApiResponse
import org.quintilis.common.service.EndpointRuleService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/routes")
class EndpointRuleController(private val endpointRuleService: EndpointRuleService): BaseController<EndpointRule, UUID, EndpointRuleDTO, EndpointRuleDTO>(endpointRuleService) {

    override val allowCreate: Boolean
        get() = false

    override val allowDelete: Boolean
        get() = false
}