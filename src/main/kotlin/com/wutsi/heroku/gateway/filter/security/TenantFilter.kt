package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.wutsi.heroku.gateway.error.ErrorURN
import com.wutsi.heroku.gateway.service.TenantExtractor
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.ForbiddenException
import com.wutsi.platform.core.tracing.TracingContext
import org.springframework.stereotype.Service

/**
 * This filter set the tenant header `X-Tenant-ID`
 */
@Service
class TenantFilter(
    private val tenantExtractor: TenantExtractor,
    private val tracingContext: TracingContext
) : AbstractSecurityFilter() {
    override fun shouldFilter(): Boolean =
        RequestContext.getCurrentContext().request.method == "POST"

    override fun run(): Any? {
        val token = getToken()
        val tenantId: Any? = token?.let { tenantExtractor.extractTenantId(it) }
            ?: tracingContext.tenantId()

        if (tenantId == null)
            throw ForbiddenException(
                error = Error(
                    code = ErrorURN.MISSING_TENANT.urn
                )
            )
        else
            RequestContext.getCurrentContext()
                .addZuulRequestHeader(TracingContext.HEADER_TENANT_ID, tenantId.toString())

        return null
    }
}
