package com.wutsi.heroku.gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.heroku.gateway.api.WutsiAccountApiCacheAware
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.WutsiAccountApiBuilder
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import org.springframework.cache.Cache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
public class AccountApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment,
    private val cache: Cache,
) {
    @Bean
    fun accountApi(): WutsiAccountApi = WutsiAccountApiCacheAware(
        cache = cache,
        mapper = mapper,
        delegate = WutsiAccountApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor
            )
        )
    )

    private fun environment(): com.wutsi.platform.account.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.platform.account.Environment.PRODUCTION
        else
            com.wutsi.platform.account.Environment.SANDBOX
}
