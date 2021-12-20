package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * This filter inspect the Authentication token in each request, and ensure that
 * - The token is valid
 * - The owner of the token is still active
 */
@Service
class AuthenticationFilter(private val keyVerifier: KeyVerifier) : ZuulFilter() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(AuthenticationFilter::class.java)
    }

    override fun shouldFilter(): Boolean =
        getToken() != null

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 0

    override fun run(): Any? {
        try {
            keyVerifier.verify(getToken()!!)
            LOGGER.info("Token is valid")
        } catch (ex: Exception) {
            LOGGER.error("Authentication failure", ex)
            RequestContext.getCurrentContext().responseStatusCode = 401
        }

        return null
    }

    private fun getToken(): String? {
        val request = RequestContext.getCurrentContext().request
        val value = request.getHeader("Authorization") ?: return null
        return if (value.startsWith("Bearer ", ignoreCase = true))
            value.substring(7)
        else
            null
    }
}
