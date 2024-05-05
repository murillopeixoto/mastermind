package org.mastermind.model

import java.util.UUID
import org.mastermind.model.Code.Peg
import model.Outcome.IN_PROGRESS
import model.Outcome.LOST
import model.Outcome.NOT_STARTED
import model.Outcome.WON
import org.mastermind.model.Feedback.Peg.BLACK
import org.mastermind.model.Feedback.Peg.WHITE
import org.mastermind.model.GameEvent.GameLost
import org.mastermind.model.GameEvent.GameStarted
import org.mastermind.model.GameEvent.GameWon
import org.mastermind.model.GameEvent.GuessMade

class Game(private val eventStream: EventStream) {
    val id: GameId = GameId(UUID.fromString(eventStream.identifier.id))

    lateinit var secret: Code
    lateinit var availablePegs: Set<Peg>
    lateinit var feedback: Feedback
    var totalAttempts = 12
    var guesses = emptyList<Guess>()
    var outcome = NOT_STARTED

    companion object {
        fun start(gameId: GameId, secret: Code, totalAttempts: Int, availablePegs: Set<Peg>): Game {
            val identifier = EventId(gameId.value)
            val eventStream = EventStream.create(identifier)
            val game = Game(eventStream)

            game.record(GameStarted.new(secret, totalAttempts, availablePegs))

            return game
        }
    }

    fun guess(guess: Guess) {
        validateGuess(guess.code)
        record(GuessMade.new(guess, feedbackOn(guess.code)))
    }

    fun lose() {
        record(GameLost.new())
    }

    fun win() {
        record(GameWon.new())
    }

    fun totalHits() = feedback.pegs.filter { it == BLACK }.size

    fun totalGuesses() = guesses.size

    private fun record(vararg events: GameEvent) {
        events.forEach {
            applyEvent(it)
            eventStream.add(it)
        }
    }

    private fun applyEvent(event: GameEvent) {
        when (event) {
            is GameStarted -> {
                secret = event.secret
                totalAttempts = event.totalAttempts
                availablePegs = event.availablePegs
                outcome = IN_PROGRESS
            }
            is GuessMade -> {
                guesses = guesses + event.guess
                feedback = event.feedback
            }
            is GameLost -> outcome = LOST
            is GameWon -> outcome = WON
        }
    }

    private fun validateGuess(guess: Code) {
        if (outcome != IN_PROGRESS) throw Exception("Game is finished: ${outcome}")
        if (isGuessTooShort(guess)) throw Exception("Guess too short")
        if (isGuessTooLong(guess)) throw Exception("Guess too long")
        if (!isGuessValid(guess)) throw Exception("Invalid Pegs")
    }

    private fun isGuessTooShort(guess: Code) = guess.length < this.secret.length
    private fun isGuessTooLong(guess: Code) = guess.length > this.secret.length
    private fun isGuessValid(guess: Code) = availablePegs.containsAll(guess.pegs)

    private fun feedbackOn(guess: Code): Feedback =
        feedbackPegsOn(guess).let { (exactHits, colourHits) -> Feedback(exactHits + colourHits) }

    private fun feedbackPegsOn(guess: Code) = totalHits(guess).map { BLACK } to colourHits(guess).map { WHITE }

    private fun totalHits(guess: Code): List<Peg> =
        secret.pegs.zip(guess.pegs).filter { (secretColour, guessColour) -> secretColour == guessColour }
            .unzip().second

    private fun colourHits(guess: Code): List<Peg> =
        secret.pegs
            .zip(guess.pegs)
            .filter { (secretColour, guessColour) -> secretColour != guessColour }
            .unzip()
            .let { (secret, guess) ->
                guess.fold(secret to emptyList<Peg>()) { (secretPegs, colourHits), guessPeg ->
                    secretPegs.remove(guessPeg)?.let { it to colourHits + guessPeg } ?: (secretPegs to colourHits)
                }.second
            }

    /**
     * Removes an element from the list and returns the new list, or null if the element wasn't found.
     */
    private fun <T> List<T>.remove(item: T): List<T>? =
        indexOf(item).let { index -> if (index != -1) filterIndexed { i, _ -> i != index } else null }
}
