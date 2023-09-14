package uk.co.mutuallyassureddistraction.paketliga.dao

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Win
import java.time.ZonedDateTime
import kotlin.test.assertEquals

class WinDaoTest {
    private lateinit var gameDao: GameDao
    private lateinit var guessDao: GuessDao
    private lateinit var target: WinDao
    private lateinit var testWrapper: DaoTestWrapper

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
        guessDao = testWrapper.buildDao(GuessDao::class.java)
        guessDao.createGuess(
            Guess(
                guessId = 1,
                gameId = 1,
                userId = "PostMasterGeneral",
                guessTime = ZonedDateTime.parse("2023-04-07T16:00:00.000Z[Europe/London]")
            )
        )

        target = testWrapper.buildDao(WinDao::class.java)
    }

    @AfterEach
    fun tearDown() {
        testWrapper.stopContainers()
    }

    @DisplayName("createGuess() will successfully insert guess into table")
    @Test
    fun canSuccessfullyInsertIntoTable() {
        val expected = createWin()
        target.addWinningGuess(expected)

        val result = testWrapper.executeSimpleQuery<Win>(
            """SELECT * FROM WIN""".trimIndent()
        )
        assertEquals(expected, result)
    }

    private fun createWin(): Win {
        val expected = Win(
            winId = 1,
            gameId = 1,
            guessId = 1,
            date = ZonedDateTime.parse("2023-04-07T12:00:00.000Z[Europe/London]")
        )

        return expected
    }
}