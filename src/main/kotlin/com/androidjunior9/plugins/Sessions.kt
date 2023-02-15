package com.androidjunior9.plugins

import com.androidjunior9.GameSession
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application.configureSessions(){
    install(Sessions){
        cookie<GameSession>("SESSION")
    }
    intercept(Plugins){
        if(call.sessions.get<GameSession>() == null){
            val username = call.parameters["username"]?:"Guest"
            val roomname = call.parameters["roomname"]?:"Guest"
            call.sessions.set(GameSession(
                username = username,
                roomname = roomname,
                sessionId = generateNonce()
            ))
        }
    }
}