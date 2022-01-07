package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.heroku.gateway.service.KeyVerifier
import com.wutsi.heroku.gateway.service.SubjectVerifier
import com.wutsi.platform.core.logging.KVLogger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class AuthenticationFilterTest {
    private lateinit var filter: AuthenticationFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var keyVerifier: KeyVerifier
    private lateinit var subjectVerifier: SubjectVerifier
    private lateinit var logger: KVLogger
    private lateinit var context: RequestContext

    @BeforeEach
    fun setUp() {
        request = mock()
        doReturn("/foo/bar").whenever(request).requestURI

        response = mock()

        context = RequestContext()
        context.request = request
        context.response = response
        RequestContext.testSetCurrentContext(context)

        keyVerifier = mock()
        subjectVerifier = mock()
        logger = mock()

        filter = AuthenticationFilter(keyVerifier, subjectVerifier, logger)
    }

    @Test
    fun `should filter when Authorization header is available`() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")
        assertTrue(filter.shouldFilter())
    }

    @Test
    fun `should not filter when Authorization header is not available`() {
        doReturn(null).whenever(request).getHeader("Authorization")
        assertFalse(filter.shouldFilter())
    }

    @Test
    fun `should not filter login request`() {
        doReturn("/login/x/y/z").whenever(request).requestURI
        assertFalse(filter.shouldFilter())
    }

    @Test
    fun filterType() {
        assertEquals("pre", filter.filterType())
    }

    @Test
    fun filterOrder() {
        assertEquals(0, filter.filterOrder())
    }

    @Test
    fun `valid token`() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")

        filter.run()

        assertTrue(context.responseStatusCode != 401)
    }

    @Test
    fun `invalid token`() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")

        val ex = RuntimeException()
        doThrow(ex).whenever(keyVerifier).verify("xxx")

        filter.run()

        assertEquals(401, context.responseStatusCode)
        verify(logger).setException(ex)
    }

    @Test
    fun `invalid subject`() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")

        val ex = RuntimeException()
        doThrow(ex).whenever(subjectVerifier).verify("xxx")

        filter.run()

        assertEquals(401, context.responseStatusCode)
        verify(logger).setException(ex)
    }
}
