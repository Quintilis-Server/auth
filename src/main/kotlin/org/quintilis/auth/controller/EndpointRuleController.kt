package org.quintilis.auth.controller

import java.util.UUID
import org.quintilis.common.controller.BaseController
import org.quintilis.common.dto.auth.EndpointRuleDTO
import org.quintilis.common.entities.auth.EndpointRule
import org.quintilis.common.service.EndpointRuleService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/routes")
class EndpointRuleController(private val endpointRuleService: EndpointRuleService) :
        BaseController<EndpointRule, UUID, EndpointRuleDTO, EndpointRuleDTO>(endpointRuleService) {

    override val allowCreate: Boolean
        get() = false

    override val allowDelete: Boolean
        get() = false
}
