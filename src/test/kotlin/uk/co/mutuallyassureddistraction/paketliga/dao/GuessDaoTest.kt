package uk.co.mutuallyassureddistraction.paketliga.dao

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

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
        val guessId = UUID.randomUUID()
        val gameId = UUID.randomUUID()
        val expected = Guess(
            guessId = guessId,
            gameId = gameId,
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
        val guessId = UUID.randomUUID()
        val gameId = UUID.randomUUID()
        val expected = Guess(
            guessId = guessId,
            gameId = gameId,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
        )
        target.createGuess(expected)

        val result = target.findGuessByGuessId(guessId)
        assertEquals(result, expected)
    }


}
