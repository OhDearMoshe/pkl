package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.PointDao
import uk.co.mutuallyassureddistraction.paketliga.dao.WinDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class GameEndServiceTest {
    private lateinit var target: GameEndService

    @BeforeEach
    fun setUp() {
        val winningGuess = getWinningGuessStub()
        val losingGuess = getLosingGuessStub()
        val searchedGame = getGameStub()

        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(999)} returns null
        every {gameDao.findActiveGameById(0)} returns searchedGame
        every {gameDao.finishGame(any(), any())} returns searchedGame

        val guessDao = mockk<GuessDao>()
        every {guessDao.findGuessesByGameId(any())} returns arrayListOf(winningGuess, losingGuess)

        val pointDao = mockk<PointDao>()
        every {pointDao.addLost(any())} returns Unit
        every {pointDao.addWin(any())} returns Unit

        val winDao = mockk<WinDao>()
        every {winDao.addWinningGuess(any())} returns Unit

        val gameResultResolver = mockk<GameResultResolver>()
        every {gameResultResolver.findWinners(any(), any()) } returns arrayListOf(winningGuess)

        target = GameEndService(guessDao, gameDao, pointDao, winDao, gameResultResolver)
    }

    @DisplayName("endGame() will return no games found string and empty array when no game is found")
    @Test
    fun returnStringWithNoGamesFound() {
        val returned = target.endGame(999, "earlier today 1pm")
        assertEquals(returned.first, "No games found.")
        assert(returned.second.isEmpty())
    }

    @DisplayName("endGame() will return null string and array of winning guesses")
    @Test
    fun returnNullStringWithWinners() {
        val returned = target.endGame(0, "earlier today 1pm")
        assertEquals(returned.first, null)
        assertEquals(returned.second.size, 1)
        assertEquals(returned.second[0].userId, "Z")
    }

    private fun getWinningGuessStub(): Guess {
        return Guess (
            guessId = 1,
            gameId = 1,
            userId = "Z",
            guessTime = ZonedDateTime.now().withHour(14).withMinute(38)
        )
    }

    private fun getLosingGuessStub(): Guess {
        return Guess (
            guessId = 2,
            gameId = 1,
            userId = "X",
            guessTime = ZonedDateTime.now().withHour(20).withMinute(38)
        )
    }

    private fun getGameStub(): Game {
        return Game(
            gameId = 1,
            gameName = "Testing testing",
            windowStart = ZonedDateTime.now().withHour(15).withMinute(0),
            windowClose = ZonedDateTime.now().withHour(19).withMinute(0),
            guessesClose = ZonedDateTime.now().withHour(14).withMinute(0),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
    }
}