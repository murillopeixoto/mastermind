import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mastermind.command.JoinGame
import org.mastermind.command.JoinGameHandler
import org.mastermind.command.MakeGuess
import org.mastermind.command.MakeGuessHandler
import org.mastermind.infrastructure.GamesInMemory
import org.mastermind.model.Code
import org.mastermind.model.Feedback
import model.Outcome.IN_PROGRESS
import model.Outcome.LOST
import model.Outcome.WON
import org.mastermind.model.Feedback.Peg.BLACK
import org.mastermind.model.GameId
import org.mastermind.model.Guess

class GameExamples {
    private val gameId = anyGameId()

    private fun anyGameId() = GameId(randomUUID())

    private val secret = Code("Red", "Green", "Blue", "Yellow")
    private val totalAttempts = 12
    private val availablePegs = setOfPegs("Red", "Green", "Blue", "Yellow", "Purple", "Pink")
    private val games = GamesInMemory()
    private val joinGameHandler = JoinGameHandler(games)
    private val makeGuessHandler = MakeGuessHandler(games)

    private fun setOfPegs(vararg pegs: String) = Code(*pegs).pegs.toSet()

    @Test
    fun `it starts the game`() {
        val command = JoinGame(gameId, secret, totalAttempts, availablePegs)
        joinGameHandler.handle(command)

        games.get(gameId).getOrNone().map { game ->
            assertEquals(game.id, gameId)
            assertEquals(game.secret, secret)
            assertEquals(game.totalAttempts, totalAttempts)
            assertEquals(game.availablePegs, availablePegs)
            assertEquals(game.guesses, emptyList())
        }
    }

    @Test
    fun `it makes a guess`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))
        val command = MakeGuess(gameId, Code("Purple", "Purple", "Purple", "Purple"))

        makeGuessHandler.handle(command)

        games.get(gameId).getOrNone().map { game ->
            assertEquals(game.guesses, listOf(Guess(command.guess)))
            assertEquals(game.outcome, IN_PROGRESS)
            assertEquals(game.feedback, Feedback())
        }
    }

    @Test
    fun `it gives feedback on the guess`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))

        val guess = Code("Red", "Purple", "Blue", "Purple")
        val command = MakeGuess(gameId, guess)

        makeGuessHandler.handle(command)

        games.get(gameId).getOrNone().map { game ->
            assertEquals(game.guesses, listOf(Guess(command.guess)))
            assertEquals(game.outcome, IN_PROGRESS)
            assertEquals(game.feedback, Feedback(listOf(BLACK, BLACK)))
        }
    }

    @Test
    fun `guesses cannot be made to non existing games`() {
        val guess = Code("Red", "Purple", "Blue", "Purple")
        val command = MakeGuess(gameId, guess)

        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }

    @Test
    fun `the guess length cannot be shorter than the secret`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))

        val guess = Code("Purple", "Purple", "Purple")
        val command = MakeGuess(gameId, guess)

        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }

    @Test
    fun `the guess length cannot be longer than the secret`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))

        val guess = Code("Purple", "Purple", "Purple", "Purple", "Purple")
        val command = MakeGuess(gameId, guess)

        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }

    @Test
    fun `it rejects pegs that the game was not started with`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))

        val guess = Code("Purple", "Purple", "Purple", "INVALID PEG")
        val command = MakeGuess(gameId, guess)

        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }

    @Test
    fun `the game is won if the secret is guessed`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))
        val command = MakeGuess(gameId, secret)

        makeGuessHandler.handle(command)

        games.get(gameId).getOrNone().map { game ->
            assertEquals(game.guesses, listOf(Guess(command.guess)))
            assertEquals(game.outcome, WON)
            assertEquals(game.feedback, Feedback(listOf(BLACK, BLACK, BLACK, BLACK)))
        }
    }

    @Test
    fun `the game can no longer be played once it's won`() {
        joinGameHandler.handle(JoinGame(gameId, secret, totalAttempts, availablePegs))
        val command = MakeGuess(gameId, secret)

        makeGuessHandler.handle(command)
        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }

    @Test
    fun `the game is lost if the secret is not guessed within the number of attempts`() {
        joinGameHandler.handle(JoinGame(gameId, secret, 1, availablePegs))
        val guess = Code("Red", "Purple", "Blue", "Purple")
        val command = MakeGuess(gameId, guess)

        makeGuessHandler.handle(command)
        games.get(gameId).getOrNone().map { game -> assertEquals(game.outcome, LOST) }
    }

    @Test
    fun `the game can no longer be played once it's lost`() {
        joinGameHandler.handle(JoinGame(gameId, secret, 1, availablePegs))
        val guess = Code("Red", "Purple", "Blue", "Purple")
        val command = MakeGuess(gameId, guess)

        makeGuessHandler.handle(command)
        assertThrows<Exception> { makeGuessHandler.handle(command) }
    }
}

infix fun <A, B> Either<A, B>.shouldSucceedWith(expected: B) =
    assertEquals(expected.right(), this, "${expected.right()} is $this")

infix fun <A, B> Either<A, B>.shouldFailWith(expected: A) =
    assertEquals(expected.left(), this, "${expected.left()} is $this")
