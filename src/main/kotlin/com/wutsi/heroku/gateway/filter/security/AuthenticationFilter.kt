package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.exception.ZuulException
import com.wutsi.heroku.gateway.service.KeyVerifier
import com.wutsi.heroku.gateway.service.SubjectVerifier
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException
import org.springframework.http.HttpStatus
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
) : AbstractSecurityFilter() {
    override fun shouldFilter(): Boolean =
        isNotLogin() && super.shouldFilter()

    override fun run(): Any? {
        val token = getToken()!!
        try {
            verifyToken(token)
            verifySubject(token)
        } catch (ex: Exception) {
            throw ZuulRuntimeException(
                ZuulException(
                    "Token not valid", HttpStatus.UNAUTHORIZED.value(), ex.message
                )
            )
        }
        return null
    }

    private fun isNotLogin(): Boolean =
        !RequestContext.getCurrentContext().request.requestURI.startsWith("/login")

    private fun verifyToken(token: String): Boolean {
        keyVerifier.verify(token)
        logger.add("token_valid", true)
        return true
    }

    private fun verifySubject(token: String): Boolean {
        subjectVerifier.verify(token)
        logger.add("subject_valid", true)
        return true
    }
}
