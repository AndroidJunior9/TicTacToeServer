package com.androidjunior9.plugins

import com.androidjunior9.authenticate
import com.androidjunior9.data.user.UserDataSource
import com.androidjunior9.security.hashing.HashingService
import com.androidjunior9.security.token.TokenConfig
import com.androidjunior9.security.token.TokenService
import com.androidjunior9.signIn
import com.androidjunior9.signUp
import com.androidjunior9.socket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {

    routing {
        signIn(userDataSource = userDataSource, hashingservice = hashingService,tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        socket()
    }
}
