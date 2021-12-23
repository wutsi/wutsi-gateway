package com.wutsi.heroku.gateway.filter.security

import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.core.test.TestRSAKeyProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class TenantExtractorTest {
    private val extractor: TenantExtractor = TenantExtractor()

    @Test
    fun `tenant-id`() {
        // THEN
        val token = createToken(1)

        assertEquals(1, extractor.extractTenantId(token))
    }

    @Test
    fun `no tenant-id`() {
        // THEN
        val token = createToken(null)

        assertNull(extractor.extractTenantId(token))
    }

    private fun createToken(tenantId: Long?): String =
        JWTBuilder(
            subject = "1",
            subjectType = SubjectType.USER,
            scope = emptyList(),
            keyProvider = TestRSAKeyProvider(),
            admin = false,
            tenantId = tenantId
        ).build()
}
