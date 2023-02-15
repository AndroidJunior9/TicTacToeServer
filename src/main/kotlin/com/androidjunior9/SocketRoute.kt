package com.androidjunior9

import com.androidjunior9.models.MakeTurn
import com.androidjunior9.models.Player
import com.androidjunior9.models.Room
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

fun Route.socket(){

        val rooms = ConcurrentHashMap<String, Room>()
        webSocket("/play") {
            print("Hello")
            val session = call.sessions.get<GameSession>()
            if(session==null){
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT,"Not able to get data about you"))
                return@webSocket
            }

            try{
                val isOldRoom = rooms.containsKey(session.roomname)
                if(isOldRoom){
                    rooms[session.roomname]?.addPlayer(
                        Player(
                            socket = this,
                            username = session.username,
                            sessionId = session.sessionId,
                            roomname = session.roomname
                        )
                    )
                    print(rooms[session.roomname]?.players)
                }else{
                    rooms[session.roomname] = Room(session.roomname)
                    rooms[session.roomname]?.addPlayer(
                        Player(
                            socket = this,
                            username = session.username,
                            sessionId = session.sessionId,
                            roomname = session.roomname
                        )
                    )
                }


                incoming.consumeEach { frame ->
                    if(frame is Frame.Text){
                        val action = extractAction(frame.readText())
                        if(rooms.containsKey(session.roomname)) {
                            rooms[session.roomname]?.finishTurn(
                                username = session.username,
                                x = action.x,
                                y = action.y
                            )
                        }
                    }
                }
            } catch (e:Exception){
                e.printStack()
            }finally{
                rooms[session.roomname]?.disconnectPlayer(session.username)
            }
        }
    }

private fun extractAction(message:String):  MakeTurn {
    val type = message.substringBefore('#')
    val body = message.substringAfter('#')

    return if(type == "make_turn"){
        Json.decodeFromString(body)
    }else MakeTurn(-1,-1)
}
