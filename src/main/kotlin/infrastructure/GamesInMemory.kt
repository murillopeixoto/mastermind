package org.mastermind.infrastructure

import arrow.core.Either
import arrow.core.raise.either
import java.util.UUID
import org.mastermind.model.Game
import org.mastermind.model.GameId
import org.mastermind.model.Games

class GamesInMemory: Games {
    private val storage: MutableMap<UUID, Game> = mutableMapOf()
    override fun get(gameId: GameId): Either<Exception, Game> = either { storage[gameId.value] ?: throw Exception("Game not found") }

    override fun add(game: Game) {
        storage[game.id.value] = game
    }
}