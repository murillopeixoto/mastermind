package org.mastermind.command

import org.mastermind.model.Code
import org.mastermind.model.Code.Peg
import org.mastermind.model.Game
import org.mastermind.model.GameId
import org.mastermind.model.Games

data class JoinGame(
    val gameId: GameId,
    val secret: Code,
    val totalAttempts: Int,
    val availablePegs: Set<Peg>
)

class JoinGameHandler(private val games: Games) {
    fun handle(command: JoinGame) {
        val game = Game.start(command.gameId, command.secret, command.totalAttempts, command.availablePegs)
        games.add(game)
    }
}
