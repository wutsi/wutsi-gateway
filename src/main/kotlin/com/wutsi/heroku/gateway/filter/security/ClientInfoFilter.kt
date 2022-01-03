package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

@Service
class ClientInfoFilter(private val logger: KVLogger) : ZuulFilter() {
    override fun shouldFilter(): Boolean =
        true

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 0

    override fun run(): Any? {
        val request = RequestContext.getCurrentContext().request

        logger.add("client_version", request.getHeader("X-Client-Version"))
        logger.add("client_os", request.getHeader("X-OS"))
        logger.add("client_os_version", request.getHeader("X-OS-Version"))
        return null
    }
}
