package com.androidjunior9

import com.androidjunior9.data.user.MongoUserDataSource
import com.androidjunior9.plugins.*
import com.androidjunior9.security.hashing.Sha256HashingService
import com.androidjunior9.security.token.JwtTokenService
import com.androidjunior9.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


fun main(args: Array<String>): Unit =
    EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module(){

    val dbName = "ktor-auth"
    val mongoPW = System.getenv("MONGO_PW")
    val db = KMongo.createClient("mongodb://shivamkadam:$mongoPW@ac-otye2ac-shard-00-00.oekdzwd.mongodb.net:27017,ac-otye2ac-shard-00-01.oekdzwd.mongodb.net:27017,ac-otye2ac-shard-00-02.oekdzwd.mongodb.net:27017/?ssl=true&replicaSet=atlas-upf2oq-shard-0&authSource=admin&retryWrites=true&w=majority").coroutine
        .getDatabase(dbName)
    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L*24L*60L*60L*1000L,
        secret = System.getenv("JWT_SECRET")

    )
    val hashingService = Sha256HashingService()


    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureSessions()
    configureRouting(
        userDataSource, hashingService, tokenService, tokenConfig
    )


    //"mongodb://shivamkadam:$mongoPW@ac-otye2ac-shard-00-00.oekdzwd.mongodb.net:27017,ac-otye2ac-shard-00-01.oekdzwd.mongodb.net:27017,ac-otye2ac-shard-00-02.oekdzwd.mongodb.net:27017/?ssl=true&replicaSet=atlas-upf2oq-shard-0&authSource=admin&retryWrites=true&w=majority"
}




