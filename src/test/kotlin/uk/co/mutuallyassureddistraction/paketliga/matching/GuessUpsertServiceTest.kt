package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.DisplayName
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import java.sql.SQLException
import kotlin.test.*

class GuessUpsertServiceTest {
    private lateinit var target: GuessUpsertService

    @DisplayName("guessGame() will create a guess successfully")
    @Test
    fun returnCreateGuessWithSuccessTrue() {
        val guessDao = mockk<GuessDao>()
        every {guessDao.createGuess(any())} returns Unit
        target = GuessUpsertService(guessDao)

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
        target = GuessUpsertService(guessDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(2, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, there is already a guess with time today 2PM", response.failMessage)
        assertEquals(2, response.gameId)
    }

    @DisplayName("guessGame() will fail to create a guess because of nonexistent Game")
    @Test
    fun returnCreateGuessWithFailedMessageDueToNonexistentGame() {
        val guessDao = mockk<GuessDao>()
        val sqlException = SQLException("Game ID does not exist", "ERRA0")
        val exception = UnableToExecuteStatementException(sqlException, null)
        every {guessDao.createGuess(any())} throws exception
        target = GuessUpsertService(guessDao)

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
        target = GuessUpsertService(guessDao)

        val response: GuessUpsertService.GuessGameResponse = target.guessGame(4, "today 2PM", "Z")
        assertFalse { response.success }
        assertEquals("Guessing failed, guess time is not between start and closing window of game #4", response.failMessage)
        assertEquals(4, response.gameId)
    }
}