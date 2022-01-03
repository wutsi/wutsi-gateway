package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertNull

internal class ClientInfoFilterTest {
    private lateinit var filter: ClientInfoFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var logger: KVLogger
    private lateinit var context: RequestContext

    @BeforeEach
    fun setUp() {
        request = mock()
        response = mock()

        context = RequestContext()
        context.request = request
        context.response = response
        RequestContext.testSetCurrentContext(context)

        logger = mock()

        filter = ClientInfoFilter(logger)
    }

    @Test
    fun shouldFilter() {
        assertTrue(filter.shouldFilter())
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
    fun `app info available`() {
        doReturn("0.0.1").whenever(request).getHeader("X-App-Version")
        doReturn("20").whenever(request).getHeader("X-App-Build-Number")
        doReturn("foo").whenever(request).getHeader(TracingContext.HEADER_CLIENT_ID)

        filter.run()

        assertEquals("foo.0.0.1.20", context.zuulRequestHeaders["X-Client-Info"])
    }

    @Test
    fun `app info not available`() {
        doReturn("foo").whenever(request).getHeader(TracingContext.HEADER_CLIENT_ID)

        filter.run()

        assertNull(context.zuulRequestHeaders["X-Client-Info"])
    }
}
