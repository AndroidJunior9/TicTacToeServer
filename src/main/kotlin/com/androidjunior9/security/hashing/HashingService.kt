package com.androidjunior9.security.hashing

interface HashingService {
    fun generateSaltedHash(value:String,saltlength:Int = 32):SaltedHash

    fun verify(value:String,saltedHash:SaltedHash):Boolean
}