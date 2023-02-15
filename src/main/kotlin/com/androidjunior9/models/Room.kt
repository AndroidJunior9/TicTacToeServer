package com.androidjunior9.models

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Room(
    private val name:String,
    val players: ConcurrentHashMap<String, Player> = ConcurrentHashMap<String,Player>()
){
    private val gameScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private var job: Job? = null
    private val  state = MutableStateFlow(GameState())
    init{
        state.onEach(::broadcast).launchIn(gameScope)
    }
    private suspend fun broadcast(state:GameState){
        players.forEach {
            it.value.socket.send(Json.encodeToString(state))
        }
    }
    fun addPlayer(player: Player):Boolean{
        if(player.roomname!=name){
            return false
        }
        when (players.size) {
            2 -> {
                return false
            }
            0 -> {
                players[player.username] = player.copy(
                    symbol = Player.SYMBOL_X
                )
                state.update {
                    it.copy(
                        players = it.players+1
                    )
                }
            }
            1 -> {
                players[player.username] = player.copy(
                    symbol = Player.SYMBOL_O
                )
                state.update {
                    it.copy(
                        players = it.players+2,
                    )
                }

            }
        }



        return true
    }

    fun finishTurn(x:Int,y:Int,username:String){
        if(state.value.board[y][x]!=0 || state.value.winner!=0) {
            return
        }
        val player = players.values.find { it.username == username }?:return
        if (player.symbol != state.value.playerAtTurn) {
            return
        }
        state.update {
            val newBoard = it.board.also { board ->
                board[y][x] = it.playerAtTurn
            }
            val isBoardFull = newBoard.all { it.all { it != 0 } }
            if(isBoardFull){
                startNewRound()
            }

            it.copy(
                board = newBoard,
                isBoardFull = isBoardFull,
                playerAtTurn = if(it.playerAtTurn==1) 2 else 1,
                winner = getWinningPlayer().also { if(it!=0) startNewRound() }

            )

        }



    }
    private fun getWinningPlayer(): Int {
        val board = state.value.board
        return if (board[0][0] != 0 && board[0][0] == board[0][1] && board[0][1] == board[0][2]) {
            board[0][0]
        }else if (board[1][0] != 0 && board[1][0] == board[1][1] && board[1][1] == board[1][2]) {
            board[1][0]
        }else if (board[2][0] != 0 && board[2][0] == board[2][1] && board[1][1] == board[2][2]) {
            board[2][0]
        }else if (board[0][0] != 0 && board[0][0] == board[1][0] && board[1][0] == board[2][0]) {
            board[0][0]
        }else if (board[0][1] != 0 && board[0][1] == board[1][1] && board[1][1] == board[2][1]) {
            board[0][1]
        }else if (board[0][2] != 0 && board[0][2] == board[1][2] && board[1][2] == board[2][2]) {
            board[0][2]
        }else if (board[0][0] != 0 && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            board[0][0]
        }else if (board[0][2] != 0 && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            board[0][0]
        }
        else{
            0
        }


    }

    fun disconnectPlayer(username: String){

        val player = players[username]?:return
        players.remove(username)
        state.update{
            it.copy(
                players = it.players-player.symbol
            )
        }
    }

    private  fun startNewRound(){
        job?.cancel()
        job = gameScope.launch {
           delay(5000L)
            if(players.size==2) {
                state.update {
                    it.copy(
                        playerAtTurn = 1,
                        winner = 0,
                        board = GameState.emptyField(),
                        isBoardFull = false
                    )
                }
            }
        }


    }

}