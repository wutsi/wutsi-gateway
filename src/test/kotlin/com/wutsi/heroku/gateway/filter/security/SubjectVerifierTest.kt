package com.wutsi.heroku.gateway.filter.security

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.core.security.KeyProvider
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.core.test.TestRSAKeyProvider
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.Application
import com.wutsi.platform.security.dto.GetApplicationResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SubjectVerifierTest {
    private lateinit var securityApi: WutsiSecurityApi
    private lateinit var accountApi: WutsiAccountApi
    private lateinit var verifier: SubjectVerifier
    private lateinit var keyProvider: KeyProvider

    @BeforeEach
    fun setUp() {
        securityApi = mock()
        accountApi = mock()

        verifier = SubjectVerifier(securityApi, accountApi)
    }

    @Test
    fun `verify active user`() {
        // GIVEN
        val account = createAccount("ACTIVE")
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(1L)

        // THEN
        val token = createToken(1, SubjectType.USER)
        verifier.verify(token)
    }

    @Test
    fun `verify inactive user`() {
        // GIVEN
        val account = createAccount("SUSPENDED")
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(1L)

        // THEN
        val token = createToken(1, SubjectType.USER)
        assertThrows<IllegalStateException> { verifier.verify(token) }
    }

    @Test
    fun `verify active app`() {
        // GIVEN
        val app = createApplication(true)
        doReturn(GetApplicationResponse(app)).whenever(securityApi).getApplication(1L)

        // THEN
        val token = createToken(1, SubjectType.APPLICATION)
        verifier.verify(token)
    }

    @Test
    fun `verify inactive app`() {
        // GIVEN
        val app = createApplication(false)
        doReturn(GetApplicationResponse(app)).whenever(securityApi).getApplication(1L)

        // THEN
        val token = createToken(1, SubjectType.APPLICATION)
        assertThrows<IllegalStateException> { verifier.verify(token) }
    }

    private fun createAccount(status: String) = Account(
        status = status
    )

    private fun createApplication(active: Boolean) = Application(
        active = active
    )

    private fun createToken(subject: Long, type: SubjectType): String =
        JWTBuilder(
            subject = subject.toString(),
            subjectType = type,
            scope = emptyList(),
            keyProvider = TestRSAKeyProvider(),
            admin = false
        ).build()
}
