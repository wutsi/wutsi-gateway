package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.wutsi.platform.core.tracing.TracingContext
import org.springframework.stereotype.Service

/**
 * This filter set the tenant header `X-Tenant-ID`
 */
@Service
class TenantFilter(
    private val tenantExtractor: TenantExtractor
) : AbstractSecurityFilter() {
    override fun run(): Any? {
        val token = getToken()!!
        val tenantId = tenantExtractor.extractTenantId(token)
        if (tenantId != null) {
            RequestContext.getCurrentContext()
                .addZuulRequestHeader(TracingContext.HEADER_TENANT_ID, tenantId.toString())
        }
        return null
    }
}
