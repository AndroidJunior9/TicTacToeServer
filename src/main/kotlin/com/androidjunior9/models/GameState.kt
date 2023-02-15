package com.androidjunior9.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val playerAtTurn:Int = Player.SYMBOL_X,
    val players:List<Int> = emptyList(),
    val isBoardFull:Boolean = false,
    val winner:Int = 0,
    val board:Array<Array<Int>> = emptyField()
){
    companion object{
        fun emptyField():Array<Array<Int>>{
            return arrayOf(
                arrayOf(0,0,0),
                arrayOf(0,0,0),
                arrayOf(0,0,0)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (playerAtTurn != other.playerAtTurn) return false
        if (players != other.players) return false
        if (isBoardFull != other.isBoardFull) return false
        if (winner != other.winner) return false
        if (!board.contentDeepEquals(other.board)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerAtTurn
        result = 31 * result + players.hashCode()
        result = 31 * result + isBoardFull.hashCode()
        result = 31 * result + winner
        result = 31 * result + board.contentDeepHashCode()
        return result
    }
}
