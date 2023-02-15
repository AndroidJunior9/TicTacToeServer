package com.androidjunior9.security.token

interface TokenService {
    fun generate(
        config:TokenConfig,
        vararg claims: TokenClaim
    ):String

}