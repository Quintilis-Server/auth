package org.quintilis.auth.repositories

import org.quintilis.auth.entities.OIDCClient
import org.quintilis.common.repositories.BaseRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OIDCRepository: BaseRepository<OIDCClient, String> {
}