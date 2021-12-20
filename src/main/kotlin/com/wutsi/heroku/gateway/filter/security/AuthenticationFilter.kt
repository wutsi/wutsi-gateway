package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.stereotype.Service

/**
 * This filter inspect the Authentication token in each request, and ensure that
 * - The token is valid
 * - The owner of the token is still active
 */
@Service
class AuthenticationFilter(
    private val keyVerifier: KeyVerifier,
    private val subjectVerifier: SubjectVerifier,
    private val logger: KVLogger
) : ZuulFilter() {
    override fun shouldFilter(): Boolean =
        getToken() != null

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 0

    override fun run(): Any? {
        val token = getToken()!!
        if (!verifyToken(token) || !verifySubject(token)) {
            RequestContext.getCurrentContext().responseStatusCode = 401
        }

        // Verify subject
        return null
    }

    private fun verifyToken(token: String): Boolean {
        try {
            keyVerifier.verify(token)
            logger.add("token_valid", true)

            return true
        } catch (ex: Exception) {
            logger.setException(ex)
            logger.add("token_valid", false)
            return false
        }
    }

    private fun verifySubject(token: String): Boolean {
        try {
            subjectVerifier.verify(token)
            logger.add("subject_valid", true)
            return true
        } catch (ex: Exception) {
            logger.setException(ex)
            logger.add("subject_valid", false)
            return false
        }
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
