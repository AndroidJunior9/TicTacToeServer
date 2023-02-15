package com.androidjunior9.models

import io.ktor.websocket.*

data class Player(
    val username:String,
    val sessionId:String,
    val roomname:String,
    val socket:WebSocketSession,
    val symbol:Int = NO_SYMBOL
){
    companion object{
        const val NO_SYMBOL = 0
        const val SYMBOL_X = 1
        const val SYMBOL_O = 2
    }
}


