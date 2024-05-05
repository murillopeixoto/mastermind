package org.mastermind.model

sealed interface GameEvent {
    val eventId: EventId

    data class GameStarted(
        override val eventId: EventId,
        val secret: Code,
        val totalAttempts: Int,
        val availablePegs: Set<Code.Peg>
    ) : GameEvent {
        companion object {
            fun new(secret: Code, totalAttempts: Int, availablePegs: Set<Code.Peg>) =
                GameStarted(EventId.generate(), secret, totalAttempts, availablePegs)
        }
    }

    data class GuessMade(
        override val eventId: EventId,
        val guess: Guess,
        val feedback: Feedback,
    ) : GameEvent {
        companion object {
            fun new(guess: Guess, feedback: Feedback) = GuessMade(EventId.generate(), guess, feedback)
        }
    }

    data class GameWon(override val eventId: EventId) : GameEvent {
        companion object {
            fun new() = GameWon(EventId.generate())
        }
    }

    data class GameLost(override val eventId: EventId) : GameEvent {
        companion object {
            fun new() = GameLost(EventId.generate())
        }
    }
}
