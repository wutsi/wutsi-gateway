package com.wutsi.heroku.gateway.filter.security

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
) : AbstractSecurityFilter() {
    override fun shouldFilter(): Boolean =
        isNotLogin() && super.shouldFilter()

    override fun run(): Any? {
        val token = getToken()!!
        if (!verifyToken(token) || !verifySubject(token)) {
            RequestContext.getCurrentContext().responseStatusCode = 401
        }

        // Verify subject
        return null
    }

    private fun isNotLogin(): Boolean =
        !RequestContext.getCurrentContext().request.requestURI.startsWith("/login")

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
}
