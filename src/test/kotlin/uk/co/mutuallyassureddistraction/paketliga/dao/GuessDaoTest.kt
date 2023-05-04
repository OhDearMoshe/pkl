package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GuessDaoTest {
    lateinit var target: GuessDao
    lateinit var testWrapper: DaoTestWrapper

    @BeforeEach
    fun setUp() {
        testWrapper = initTests()
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
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
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
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = target.findGuessByGuessId(1)
        assertEquals(result, expected)
    }

    @DisplayName("createGuess() will fail to insert on non-unique guess")
    @Test
    fun failToInsertOnConstraintViolation() {
        val expectedOne = Guess(
            guessId = 1,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
        )
        target.createGuess(expectedOne)

        val expectedTwo = Guess(
            guessId = 2,
            gameId = 1,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
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
}
