package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.heroku.gateway.error.ErrorURN
import com.wutsi.heroku.gateway.service.TenantExtractor
import com.wutsi.platform.core.error.exception.ForbiddenException
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertFalse

internal class TenantFilterTest {
    private lateinit var tenantExtractor: TenantExtractor
    private lateinit var filter: TenantFilter
    private lateinit var context: RequestContext
    private lateinit var request: HttpServletRequest
    private lateinit var tracingContext: TracingContext

    @BeforeEach
    fun setUp() {
        tracingContext = mock()
        tenantExtractor = mock()
        filter = TenantFilter(tenantExtractor, tracingContext)

        request = mock()
        context = mock()
        doReturn(request).whenever(context).request
        RequestContext.testSetCurrentContext(context)
    }

    @Test
    fun shouldFilterPOST() {
        doReturn("POST").whenever(request).method
        assertTrue(filter.shouldFilter())
    }

    @Test
    fun shouldFilterGET() {
        doReturn("GET").whenever(request).method
        assertFalse(filter.shouldFilter())
    }

    @Test
    fun shouldFilterOPTION() {
        doReturn("OPTION").whenever(request).method
        assertFalse(filter.shouldFilter())
    }

    @Test
    fun withTenantIdInToken() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")
        doReturn(111L).whenever(tenantExtractor).extractTenantId(any())

        filter.run()

        verify(context).addZuulRequestHeader(TracingContext.HEADER_TENANT_ID, "111")
    }

    @Test
    fun withTenantIdInContext() {
        doReturn(null).whenever(request).getHeader("Authorization")
        doReturn("111").whenever(tracingContext).tenantId()

        filter.run()

        verify(context).addZuulRequestHeader(TracingContext.HEADER_TENANT_ID, "111")
    }

    @Test
    fun withoutTenant() {
        doReturn(null).whenever(request).getHeader("Authorization")
        doReturn(null).whenever(tracingContext).tenantId()

        val ex = assertThrows<ForbiddenException> {
            filter.run()
        }
        assertEquals(ErrorURN.MISSING_TENANT.urn, ex.error.code)
    }
}
