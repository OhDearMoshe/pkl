package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.DisplayName
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.sql.SQLException
import java.time.ZonedDateTime
import kotlin.test.*

class GuessUpsertServiceTest {
    private lateinit var target: GuessUpsertService

    private val javaDtf: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yy HH:mm")

    @DisplayName("guessGame() will create a guess successfully")
    @Test
    fun returnCreateGuessWithSuccessTrue() {
        val guessDao = mockk<GuessDao>()
        every {guessDao.createGuess(any())} returns Unit
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns getGameStub()
        target = GuessUpsertService(guessDao, gameDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(1, "today 2PM", "Z")
        assertTrue { response.success }
        assertNull(response.failMessage)
        assertEquals(1, response.gameId)
    }

    @DisplayName("guessGame() will fail to create a guess because of duplicate guess time")
    @Test
    fun returnCreateGuessWithFailedMessageDueToDuplicateGuessTime() {
        val guessDao = mockk<GuessDao>()
        val sqlException = SQLException("Guess time has already exist", "23505")
        val exception = UnableToExecuteStatementException(sqlException, null)
        every {guessDao.createGuess(any())} throws exception
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns getGameStub()
        target = GuessUpsertService(guessDao, gameDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(2, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, there is already a guess with time today 2PM", response.failMessage)
        assertEquals(2, response.gameId)
    }

    @DisplayName("guessGame() will fail to create a guess because of nonexistent Game")
    @Test
    fun returnCreateGuessWithFailedMessageDueToNonexistentGame() {
        val guessDao = mockk<GuessDao>()
        every {guessDao.createGuess(any())} returns Unit
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns null
        target = GuessUpsertService(guessDao, gameDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(3, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, there is no active game with game ID #3", response.failMessage)
        assertEquals(3, response.gameId)
    }

    @DisplayName("guessGame() will fail to create a guess because guess time is not between guess window time")
    @Test
    fun returnCreateGuessWithFailedMessageDueToGuessTimeOutOfRange() {
        val guessDao = mockk<GuessDao>()
        val sqlException = SQLException("Guess time is not between start and closing window range of the game", "ERRA1")
        val exception = UnableToExecuteStatementException(sqlException, null)
        every {guessDao.createGuess(any())} throws exception
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns getGameStub()
        target = GuessUpsertService(guessDao, gameDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(4, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, guess time is not between start and closing window of game #4", response.failMessage)
        assertEquals(4, response.gameId)
    }

    @DisplayName("guessGame() will fail to create a guess because guessing deadline has passed")
    @Test
    fun returnCreateGuessWithFailedMessageDueToPassedGuessingDeadline() {
        val guessDao = mockk<GuessDao>()
        every {guessDao.createGuess(any())} returns Unit
        val gameStub = getGameStub()
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns gameStub

        val mockZonedDateTime = ZonedDateTime.now().withHour(18).withMinute(1)
        mockkStatic(ZonedDateTime::class)
        every {ZonedDateTime.now()} returns mockZonedDateTime
        target = GuessUpsertService(guessDao, gameDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(3, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, guessing deadline for game #3 has passed, guessing deadline was at " +
            gameStub.guessesClose.format(javaDtf), response.failMessage)
        assertEquals(3, response.gameId)

        unmockkStatic(ZonedDateTime::class)
    }

    private fun getGameStub(): Game {
        return Game(
            gameId = 1,
            gameName = "Testing testing",
            windowStart = ZonedDateTime.now().withHour(11).withMinute(0),
            windowClose = ZonedDateTime.now().withHour(19).withMinute(0),
            guessesClose = ZonedDateTime.now().withHour(18).withMinute(0),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
    }
}