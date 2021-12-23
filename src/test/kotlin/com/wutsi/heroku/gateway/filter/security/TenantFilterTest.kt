package com.wutsi.heroku.gateway.filter.security

import com.netflix.zuul.context.RequestContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.core.tracing.TracingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.servlet.http.HttpServletRequest

internal class TenantFilterTest {
    private lateinit var tenantExtractor: TenantExtractor
    private lateinit var filter: TenantFilter
    private lateinit var context: RequestContext
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setUp() {
        tenantExtractor = mock()
        filter = TenantFilter(tenantExtractor)

        request = mock()
        context = mock()
        doReturn(request).whenever(context).request
        RequestContext.testSetCurrentContext(context)
    }

    @Test
    fun withTenantId() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")
        doReturn(111L).whenever(tenantExtractor).extractTenantId(any())

        filter.run()

        verify(context).addZuulRequestHeader(TracingContext.HEADER_TENANT_ID, "111")
    }

    @Test
    fun noTenantId() {
        doReturn("Bearer xxx").whenever(request).getHeader("Authorization")
        doReturn(null).whenever(tenantExtractor).extractTenantId(any())

        filter.run()

        verify(context, never()).addZuulRequestHeader(eq(TracingContext.HEADER_TENANT_ID), any())
    }
}
