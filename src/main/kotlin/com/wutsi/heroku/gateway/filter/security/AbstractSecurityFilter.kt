package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext

abstract class AbstractSecurityFilter : ZuulFilter() {
    override fun shouldFilter(): Boolean =
        getToken() != null

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 0

    protected fun getToken(): String? {
        val request = RequestContext.getCurrentContext().request
        val value = request.getHeader("Authorization") ?: return null
        return if (value.startsWith("Bearer ", ignoreCase = true))
            value.substring(7)
        else
            null
    }
}
