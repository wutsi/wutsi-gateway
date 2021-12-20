package com.wutsi.heroku.gateway.filter.security

import com.auth0.jwt.JWT
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.security.WutsiSecurityApi
import org.springframework.stereotype.Service

@Service
open class SubjectVerifier(
    private val securityApi: WutsiSecurityApi,
    private val accountApi: WutsiAccountApi
) {
    fun verify(key: String) {
        val jwt = JWT.decode(key)
        val type = jwt.claims[JWTBuilder.CLAIM_SUBJECT_TYPE]
        if (SubjectType.APPLICATION.name.equals(type?.asString(), true)) {
            validateApplication(jwt.subject.toLong())
        } else if (SubjectType.USER.name.equals(type?.asString(), true)) {
            validateAccount(jwt.subject.toLong())
        } else
            throw IllegalStateException("Invalid subject_type: $type")
    }

    private fun validateApplication(id: Long) {
        val app = securityApi.getApplication(id).application
        if (!app.active)
            throw IllegalStateException("Application$id is not active")
    }

    private fun validateAccount(id: Long) {
        val account = accountApi.getAccount(id).account
        if (account.status != "ACTIVE")
            throw IllegalStateException("User$id is not active")
    }
}
