package com.wutsi.heroku.gateway.api

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.dto.AuthenticationRequest
import com.wutsi.platform.security.dto.AuthenticationResponse
import com.wutsi.platform.security.dto.CreateApplicationRequest
import com.wutsi.platform.security.dto.CreateApplicationResponse
import com.wutsi.platform.security.dto.CreateKeyResponse
import com.wutsi.platform.security.dto.CreateScopeRequest
import com.wutsi.platform.security.dto.CreateScopeResponse
import com.wutsi.platform.security.dto.GetApplicationResponse
import com.wutsi.platform.security.dto.GetKeyResponse
import com.wutsi.platform.security.dto.GrantScopeRequest
import com.wutsi.platform.security.dto.SearchApplicationResponse
import com.wutsi.platform.security.dto.SearchScopeResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.Cache

internal class WutsiSecurityApiCacheAwareTest {
    private lateinit var delegate: WutsiSecurityApi
    private lateinit var cache: Cache
    private lateinit var api: WutsiSecurityApiCacheAware

    @BeforeEach
    fun setUp() {
        delegate = mock()
        cache = mock()
        api = WutsiSecurityApiCacheAware(delegate, cache)
    }

    @Test
    fun authenticate() {
        val request = AuthenticationRequest()
        val result = AuthenticationResponse()
        doReturn(result).whenever(delegate).authenticate(request)

        val response = api.authenticate(request)

        assertEquals(result, response)
    }

    @Test
    fun application() {
        val result = GetApplicationResponse()
        doReturn(result).whenever(delegate).application("xxx")

        val response = api.application("xxx")

        assertEquals(result, response)
    }

    @Test
    fun createApplication() {
        val request = CreateApplicationRequest()
        val result = CreateApplicationResponse()
        doReturn(result).whenever(delegate).createApplication(request)

        val response = api.createApplication(request)

        assertEquals(result, response)
    }

    @Test
    fun createKey() {
        val result = CreateKeyResponse()
        doReturn(result).whenever(delegate).createKey()

        val response = api.createKey()

        assertEquals(result, response)
    }

    @Test
    fun createScope() {
        val request = CreateScopeRequest()
        val result = CreateScopeResponse()
        doReturn(result).whenever(delegate).createScope(request)

        val response = api.createScope(request)

        assertEquals(result, response)
    }

    @Test
    fun getApplicationFromServer() {
        doReturn(null).whenever(cache).get("application_1", GetApplicationResponse::class.java)

        val result = GetApplicationResponse()
        doReturn(result).whenever(delegate).getApplication(1)

        val response = api.getApplication(1)

        assertEquals(result, response)
        verify(cache).put("application_1", response)
    }

    @Test
    fun getApplicationFromCache() {
        val result = GetApplicationResponse()
        doReturn(result).whenever(cache).get("application_1", GetApplicationResponse::class.java)

        val response = api.getApplication(1)

        assertEquals(result, response)
        verify(cache, never()).put("application_1", response)
        verify(delegate, never()).getApplication(1)
    }

    @Test
    fun getKeyFromServer() {
        doReturn(null).whenever(cache).get("key_1", GetKeyResponse::class.java)

        val result = GetKeyResponse()
        doReturn(result).whenever(delegate).getKey(1)

        val response = api.getKey(1)

        assertEquals(result, response)
        verify(cache).put("key_1", response)
    }

    @Test
    fun getKeyFromCache() {
        val result = GetKeyResponse()
        doReturn(result).whenever(cache).get("key_1", GetKeyResponse::class.java)

        val response = api.getKey(1)

        assertEquals(result, response)
        verify(cache, never()).put("key_1", response)
        verify(delegate, never()).getKey(1)
    }

    @Test
    fun grantScopes() {
        val request = GrantScopeRequest()
        api.grantScopes(1, request)

        verify(delegate).grantScopes(1, request)
    }

    @Test
    fun searchApplications() {
        val result = SearchApplicationResponse()
        doReturn(result).whenever(delegate).searchApplications("xxx", 1, 100)

        val response = api.searchApplications("xxx", 1, 100)

        assertEquals(result, response)
    }

    @Test
    fun searchScopes() {
        val result = SearchScopeResponse()
        doReturn(result).whenever(delegate).searchScopes()

        val response = api.searchScopes()

        assertEquals(result, response)
    }
}
