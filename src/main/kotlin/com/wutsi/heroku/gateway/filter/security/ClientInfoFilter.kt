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
        val version = request.getHeader("X-Client-Version")
        val os = request.getHeader("X-OS")
        val clientId = request.getHeader(TracingContext.HEADER_CLIENT_ID)
        val clientInfo = "$clientId-$version-$os".lowercase()

        logger.add("client_version", version)
        logger.add("client_os", os)
        logger.add("client_os_version", request.getHeader("X-OS-Version"))
        logger.add("client_info", clientInfo)

        RequestContext.getCurrentContext()
            .addZuulRequestHeader(TracingContext.HEADER_CLIENT_INFO, clientInfo)

        return null
    }
}
