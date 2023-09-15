package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GuessDaoTest {
    lateinit var gameDao: GameDao
    lateinit var target: GuessDao
    lateinit var testWrapper: DaoTestWrapper

    @BeforeEach
    fun setUp() {
        testWrapper = initTests()
        gameDao = testWrapper.buildDao(GameDao::class.java)
        gameDao.createGame(
            Game(
                gameId = 1,
                gameName = "random game name",
                windowStart = ZonedDateTime.parse("2023-04-07T09:00:00.000Z[Europe/London]"),
                windowClose = ZonedDateTime.parse("2023-04-07T17:00:00.000Z[Europe/London]"),
                guessesClose = ZonedDateTime.parse("2023-04-07T12:00:00.000Z[Europe/London]"),
                deliveryTime = null,
                userId = "Z",
                gameActive = true
            )
        )

        target = testWrapper.buildDao(GuessDao::class.java)
    }

    @AfterEach
    fun tearDown() {
        testWrapper.stopContainers()
    }

    @DisplayName("createGuess() will successfully inset guess into table")
    @Test
    fun canSuccessfullyInsertIntoTable() {
        val expected = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = testWrapper.executeSimpleQuery<Guess>(
            """
            SELECT 
                guessId,
                gameId,
                userId,
                guessTime
            FROM GUESS
        """.trimIndent())
        assertEquals(result, expected)
    }

    @DisplayName("findGuessByGuessId() will successfully return a guess found by id")
    @Test
    fun canSuccessfullyReadyFromTableByGuessId() {
        val expected = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = target.findGuessByGuessId(1)
        assertEquals(result, expected)
    }

    @DisplayName("createGuess() will fail to insert when no gameId found in the Game table")
    @Test
    fun failToInsertOnGameIdForeignKeyTriggerViolation() {
        val expectedOne = Guess(
            guessId = 1,
            gameId = 999,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )

        // SQLSTATE 23503: The insert or update value of a foreign key is invalid
        try {
            target.createGuess(expectedOne)
        } catch(e: UnableToExecuteStatementException) {
            val original = e.cause
            assertIs<PSQLException>(original)
            assertEquals("ERRA0", original.sqlState)
        }
    }

    @DisplayName("createGuess() will fail to insert on non-unique guess")
    @Test
    fun failToInsertOnGuessTimeConstraintViolation() {
        val expectedOne = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expectedOne)

        val expectedTwo = Guess(
            guessId = 2,
            gameId = 1,
            userId = "Z",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )

        // SQLSTATE 23505:
        // A violation of the constraint imposed by a unique index or a unique constraint occurred.
        try {
            target.createGuess(expectedTwo)
        } catch(e: UnableToExecuteStatementException) {
            val original = e.cause
            assertIs<PSQLException>(original)
            assertEquals("23505", original.sqlState)
        }
    }

    @DisplayName("createGuess() will fail to insert when the guessTime is not between guess window range")
    @Test
    fun failToInsertOnGuessOutOfWindowRangeTriggerViolation() {
        val expectedOne = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T20:00:00.000Z[Europe/London]")
        )

        try {
            target.createGuess(expectedOne)
        } catch(e: UnableToExecuteStatementException) {
            val original = e.cause
            assertIs<PSQLException>(original)
            assertEquals("ERRA1", original.sqlState)
        }
    }

    @DisplayName("findGuessesByGameId() will return a list of guesses")
    @Test
    fun canSuccessfullyFindGuessesGivenGameId() {
        val expected = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = target.findGuessesByGameId(1)
        assertEquals(result, listOf(expected))
    }

    @DisplayName("findGuessesByGameId() will return empty list on wrong game Id")
    @Test
    fun canSuccessfullyReturnEmptyListGivenWrongGameId() {
        val expected = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = target.findGuessesByGameId(999)
        assertTrue { result.isEmpty() }
    }

    @DisplayName("createGuess() will update data if user has guessed before")
    @Test
    fun canSuccessfullyUpdateDataWhenUserHasGuessedBefore() {
        val expected = Guess(
            null,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected)

        val expected2 = Guess(
            null,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-04-07T17:00:00.000Z[Europe/London]")
        )
        target.createGuess(expected2)

        val result = target.findGuessesByGameId(1)
        assertEquals(result.size, 1)
        assertEquals(result[0].userId, expected2.userId)
        assertEquals(result[0].gameId, expected2.gameId)
        assertEquals(result[0].guessTime, expected2.guessTime)
    }
}
