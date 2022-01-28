package com.wutsi.heroku.gateway.api

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
import org.slf4j.LoggerFactory
import org.springframework.cache.Cache

class WutsiSecurityApiCacheAware(
    private val delegate: WutsiSecurityApi,
    private val cache: Cache
) : WutsiSecurityApi {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(WutsiSecurityApiCacheAware::class.java)
    }

    override fun authenticate(request: AuthenticationRequest): AuthenticationResponse =
        delegate.authenticate(request)

    override fun createApplication(request: CreateApplicationRequest): CreateApplicationResponse =
        delegate.createApplication(request)

    override fun createKey(): CreateKeyResponse =
        delegate.createKey()

    override fun createScope(request: CreateScopeRequest): CreateScopeResponse =
        delegate.createScope(request)

    override fun logout() {
        delegate.logout()
    }

    override fun getApplication(id: Long): GetApplicationResponse {
        val key = getApplicationCacheKey(id)
        val cached = getFromCache(key, GetApplicationResponse::class.java)
        if (cached != null)
            return cached

        val response = delegate.getApplication(id)
        putToCache(key, response)
        return response
    }

    override fun getKey(id: Long): GetKeyResponse {
        val key = getKeyCacheKey(id)
        val cached = getFromCache(key, GetKeyResponse::class.java)
        if (cached != null)
            return cached

        val response = delegate.getKey(id)
        putToCache(key, response)
        return response
    }

    override fun grantScopes(id: Long, request: GrantScopeRequest) =
        delegate.grantScopes(id, request)

    override fun searchApplications(name: String?, limit: Int, offset: Int): SearchApplicationResponse =
        delegate.searchApplications(name, limit, offset)

    override fun searchScopes(): SearchScopeResponse =
        delegate.searchScopes()

    private fun getApplicationCacheKey(id: Long): String =
        "application_$id"

    private fun getKeyCacheKey(id: Long): String =
        "key_$id"

    private fun <T> getFromCache(key: String, type: Class<T>): T? {
        try {
            return cache.get(key, type)
        } catch (ex: Exception) {
            LOGGER.warn("Unable to get from cache: $key", ex)
            return null
        }
    }

    private fun putToCache(key: String, value: Any) {
        try {
            return cache.put(key, value)
        } catch (ex: Exception) {
            LOGGER.warn("Unable to store into cache: $key", ex)
        }
    }
}
