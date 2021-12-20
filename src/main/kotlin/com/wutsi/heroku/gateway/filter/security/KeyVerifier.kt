package com.wutsi.heroku.gateway.filter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.wutsi.platform.core.security.KeyProvider
import org.springframework.stereotype.Service
import java.security.interfaces.RSAPublicKey

@Service
open class KeyVerifier(private val keyProvider: KeyProvider) {
    fun verify(key: String) {
        val algorithm = getAlgorithm(key)
        JWT.require(algorithm)
            .build()
            .verify(key)
    }

    private fun getAlgorithm(jwt: String): Algorithm {
        val token = JWT.decode(jwt)
        if (token.algorithm == "RS256") {
            val key = keyProvider.getKey(token.keyId)
            return Algorithm.RSA256(key as RSAPublicKey, null)
        }

        throw IllegalStateException("Encryption algorithm not supported: ${token.algorithm}")
    }
}
