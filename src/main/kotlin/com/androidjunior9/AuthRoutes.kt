package com.androidjunior9

import com.androidjunior9.data.request.AuthRequest
import com.androidjunior9.data.responses.AuthResponse
import com.androidjunior9.data.user.User
import com.androidjunior9.data.user.UserDataSource
import com.androidjunior9.security.hashing.HashingService
import com.androidjunior9.security.hashing.SaltedHash
import com.androidjunior9.security.token.TokenClaim
import com.androidjunior9.security.token.TokenConfig
import com.androidjunior9.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.codec.digest.DigestUtils

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource,
){
    post("signup"){
        print("Hi")
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull() ?:kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        println("${request.username}${request.password}")

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPWTooShort = request.password.length < 8
        if(areFieldsBlank || isPWTooShort){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,

        )

        val wasAcKnowledged = userDataSource.insertNewUser(user)
        if(!wasAcKnowledged){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingservice:HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig

){
    post("signin"){
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull() ?:kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        println("${request.username}${request.password}")

        val user = userDataSource.getUserByUsername(request.username)
        if(user == null){
            print("Null")
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        print("NotNull")
        val isValidePassword = hashingservice.verify(
            request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidePassword){
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            println("Upassword: ${user.password} Usalt: ${user.salt} reqpass: ${request.password}")
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val token = tokenService.generate(
            tokenConfig,
            TokenClaim(
                name = "userName",
                value = user.username
            )
        )
        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
        print(token)
    }
}

fun Route.authenticate(){
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}



