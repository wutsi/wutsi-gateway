package com.wutsi.heroku.gateway.filter.security

import com.wutsi.platform.core.security.KeyProvider
import com.wutsi.platform.security.WutsiSecurityApi
import org.springframework.cache.Cache
import org.springframework.stereotype.Service
import java.security.Key
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Service
class WutsiKeyProvider(
    private val securityApi: WutsiSecurityApi,
    private val cache: Cache,
) : KeyProvider {
    companion object {
        const val ALGORITHM = "RSA"
    }

    override fun getKey(id: String): Key {
        // Load from cache
        val cacheKey = cacheKey(id)
        val keyContent = cache.get(cacheKey, String::class.java)
        if (keyContent != null)
            return toPublicKey(keyContent, ALGORITHM)

        // Load from server
        val key = securityApi.getKey(id.toLong()).key
        if (key.algorithm == ALGORITHM) {
            cache.put(cacheKey, key.content)
            return toPublicKey(key.content, key.algorithm)
        } else {
            throw IllegalStateException("Algorithm not supported: ${key.algorithm}")
        }
    }

    private fun toPublicKey(content: String, algorithm: String): PublicKey {
        val byteKey: ByteArray = Base64.getDecoder().decode(content)
        val x509publicKey = X509EncodedKeySpec(byteKey)
        val kf = KeyFactory.getInstance(algorithm)
        return kf.generatePublic(x509publicKey)
    }

    private fun cacheKey(id: String): String =
        "key_$id"
}
