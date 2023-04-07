package uk.co.mutuallyassureddistraction.paketliga.dao

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

class GameDaoTest {
    private lateinit var target: GameDao
    private lateinit var testWrapper: DaoTestWrapper

    @BeforeEach
    fun setUp() {
        testWrapper = initTests()
        target = testWrapper.buildDao(GameDao::class.java)
    }

    @AfterEach
    fun tearDown() {
        testWrapper.stopContainers()
    }

    @DisplayName("createGame() will successfully insert a game into the table")
    @Test
    fun canSuccessfullyInsertIntoTable() {
        val createdGame: Game = createGame();

        val result = testWrapper.executeSimpleQuery<Game>(
            """SELECT * FROM GAME""".trimIndent()
        )
        assertEquals(result, createdGame)
    }

    @DisplayName("createGame() will successfully insert a game into the table")
    @Test
    fun canSuccessfullyFindActiveGameByName() {
        val createdGame: Game = createGame();

        val searchedGame: Game = target.findActiveGameByName("random");

        assertEquals(createdGame, searchedGame);
    }

    private fun createGame(): Game {
        val gameId = UUID.randomUUID()
        val gameName = "A random game name for test";

        val expected = Game(
            gameId = gameId,
            gameName = gameName,
            windowStart = ZonedDateTime.parse("2023-04-07T09:00:00.000Z[Europe/London]"),
            windowClose = ZonedDateTime.parse("2023-04-07T17:00:00.000Z[Europe/London]"),
            guessesClose = ZonedDateTime.parse("2023-04-07T12:00:00.000Z[Europe/London]"),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
        target.createGame(expected)

        return expected;
    }

}

