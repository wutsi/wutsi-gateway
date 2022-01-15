package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

        context = mock()
        doReturn(request).whenever(context).request
        doReturn(response).whenever(context).response
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
    fun `log app info`() {
        doReturn("foo").whenever(request).getHeader(TracingContext.HEADER_CLIENT_ID)
        doReturn("Android").whenever(request).getHeader("X-OS")
        doReturn("10").whenever(request).getHeader("X-OS-Version")
        doReturn("0.0.1.20").whenever(request).getHeader("X-Client-Version")

        filter.run()

        verify(logger).add("client_version", "0.0.1.20")
        verify(logger).add("client_os", "Android")
        verify(logger).add("client_os_version", "10")
        verify(logger).add("client_info", "foo-0.0.1.20-android")

        verify(context).addZuulRequestHeader(TracingContext.HEADER_CLIENT_INFO, "foo-0.0.1.20-android")
    }
}
