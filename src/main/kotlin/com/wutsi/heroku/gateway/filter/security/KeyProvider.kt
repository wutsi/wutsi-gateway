package com.wutsi.heroku.gateway.filter.security

import java.security.Key

interface KeyProvider {
    fun getKey(id: String): Key
}
