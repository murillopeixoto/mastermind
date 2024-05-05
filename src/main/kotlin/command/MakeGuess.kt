package org.mastermind.command

import org.mastermind.model.Code
import org.mastermind.model.GameId
import org.mastermind.model.Games
import org.mastermind.model.Guess

data class MakeGuess(val gameId: GameId, val guess: Code)

class MakeGuessHandler(private val games: Games) {
    fun handle(command: MakeGuess) {
        games.get(command.gameId).map { game ->
            game.guess(Guess(command.guess))

            if (game.totalHits() == game.secret.length) game.win()
            if (game.totalGuesses() >= game.totalAttempts) game.lose()

            games.add(game)
        }
    }
}
