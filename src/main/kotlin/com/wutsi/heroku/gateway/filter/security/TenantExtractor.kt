package com.wutsi.heroku.gateway.filter.security

import com.auth0.jwt.JWT
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import org.springframework.stereotype.Service

@Service
open class TenantExtractor {
    fun extractTenantId(key: String): Long? {
        val jwt = JWT.decode(key)
        return jwt.claims[JWTBuilder.CLAIM_TENANT_ID]?.asLong()
    }
}
