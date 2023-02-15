package com.androidjunior9.security.token

data class TokenConfig(
    val issuer:String,
    val audience:String,
    val expiresIn:Long,
    val secret:String
)
