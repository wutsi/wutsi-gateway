package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.tracing.TracingContext
import org.springframework.stereotype.Service

@Service
class ClientInfoFilter(private val logger: KVLogger) : ZuulFilter() {
    override fun shouldFilter(): Boolean =
        true

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 0

    override fun run(): Any? {
        val request = RequestContext.getCurrentContext().request
        val name = request.getHeader("X-App-Name")
        val version = request.getHeader("X-App-Version")
        val buildNumber = request.getHeader("X-App-Build-Number")
        val clientId = request.getHeader(TracingContext.HEADER_CLIENT_ID)

        logger.add("app_name", name)
        logger.add("app_version", version)
        logger.add("app_build_number", buildNumber)

        if (version != null && buildNumber != null && clientId != null) {
            val clientInfo = "$clientId.$version.$buildNumber"

            logger.add("client_info", clientInfo)
            RequestContext.getCurrentContext().zuulRequestHeaders["X-Client-Info"] = clientInfo
        }
        return null
    }
}
