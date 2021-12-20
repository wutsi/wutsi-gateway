package com.wutsi.heroku.gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.heroku.gateway.api.WutsiSecurityApiCacheAware
import com.wutsi.platform.core.security.feign.FeignApiKeyRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import com.wutsi.platform.security.WutsiSecurityApi
import com.wutsi.platform.security.WutsiSecurityApiBuilder
import org.springframework.cache.Cache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class SecurityApiConfiguration(
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val apiKeyRequestInterceptor: FeignApiKeyRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment,
    private val cache: Cache,
) {
    @Bean
    fun securityApi(): WutsiSecurityApi = WutsiSecurityApiCacheAware(
        cache = cache,
        delegate = WutsiSecurityApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                apiKeyRequestInterceptor,
                tracingRequestInterceptor,
            )
        )
    )

    private fun environment(): com.wutsi.platform.security.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.platform.security.Environment.PRODUCTION
        else
            com.wutsi.platform.security.Environment.SANDBOX
}
