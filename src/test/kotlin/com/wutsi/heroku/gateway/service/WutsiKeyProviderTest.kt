package com.wutsi.heroku.gateway.service

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.GetKeyResponse
import com.wutsi.platform.security.dto.Key
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Base64
import kotlin.test.assertEquals

internal class WutsiKeyProviderTest {
    private lateinit var securityApi: WutsiSecurityApi
    private lateinit var provider: WutsiKeyProvider

    val content =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs9h3/n6Zr3P6EMZmcLH5JicO+y4cicNAiF1h6FB911MD1sw1ygcMDm9Wto/5y7o/BrIc8xZ8OYUCAw9QwW+FwumOdZTHQPbm08AAniVpWAGgRNh+u2X9GUYzEZ0iRyN5tlVXPAQktjGb/vo9v0Jq9eAhw8xrCWuZLbKSgyfJqC+KQCa98jJvpeeqKJptHBjangEI5FmvIJfSN/ezwARNrryUYjDDY4BpT6whXBkHEVw+r4SKKW8MRz7Sca4rTOxXiyQKUfzU7IobxW4KDxvGJ4S6DJ3UF07IX6YUSYmo6ptCMRoZ+KGEDpGiC06BMC2XtrkiPx/PeBB3+0wovsfEtQIDAQAB"

    @BeforeEach
    fun setUp() {
        securityApi = mock()
        provider = WutsiKeyProvider(securityApi)
    }

    @Test
    fun `load key from server`() {
        val response = GetKeyResponse(
            key = Key(
                algorithm = "RSA",
                content = content
            )
        )
        doReturn(response).whenever(securityApi).getKey(1)

        val key = provider.getKey("1")
        assertEquals(response.key.content, Base64.getEncoder().encodeToString(key.encoded))
    }

    @Test
    fun `load key from server with unsupported algo`() {
        val response = GetKeyResponse(
            key = Key(
                algorithm = "ECDSA",
                content = "yo-man"
            )
        )
        doReturn(response).whenever(securityApi).getKey(1)

        assertThrows<IllegalStateException> {
            provider.getKey("1")
        }
    }
}
