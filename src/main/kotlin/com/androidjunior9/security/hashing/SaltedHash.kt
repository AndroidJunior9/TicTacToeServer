package com.androidjunior9.security.hashing

data class SaltedHash(
    val hash:String,
    val salt:String
)
