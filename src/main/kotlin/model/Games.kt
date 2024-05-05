package org.mastermind.model

import arrow.core.Either

interface Games {
    fun get(gameId: GameId): Either<Exception, Game>
    fun add(game: Game)
}
