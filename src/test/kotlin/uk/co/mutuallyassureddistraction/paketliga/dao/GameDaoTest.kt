package uk.co.mutuallyassureddistraction.paketliga.dao

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameDaoTest {
    private lateinit var target: GameDao
    private lateinit var testWrapper: DaoTestWrapper
    private lateinit var createdGame: Game

    @BeforeEach
    fun setUp() {
        testWrapper = initTests()
        target = testWrapper.buildDao(GameDao::class.java)
        createdGame = createGame()
    }

    @AfterEach
    fun tearDown() {
        testWrapper.stopContainers()
    }

    @DisplayName("createGame() will successfully insert a game into the table")
    @Test
    fun canSuccessfullyInsertIntoTable() {
        val result = testWrapper.executeSimpleQuery<Game>(
            """SELECT * FROM GAME""".trimIndent()
        )
        assertEquals(result, createdGame)
    }

    @DisplayName("findActiveGameByName() will successfully return a game by name")
    @Test
    fun canSuccessfullyFindActiveGameByName() {
        val searchedGame: Game = target.findActiveGameByName("random")
        assertEquals(createdGame, searchedGame)
    }

    @DisplayName("findActiveGameById() will successfully return a game by id")
    @Test
    fun canSuccessfullyFindActiveGameById() {
        val searchedGame: Game? = target.findActiveGameById(1)
        assertEquals(createdGame, searchedGame)
    }

    @DisplayName("findActiveGameById() will return null on unknown gameId")
    @Test
    fun getNullOnWrongGameIdInFindActiveGameById() {
        val searchedGame: Game? = target.findActiveGameById(999)
        assertNull(searchedGame)
    }

    @DisplayName("updateGameTimes() will successfully update the game time if not null")
    @Test
    fun canSuccessfullyUpdateGameTimes() {
        val updatedGame: Game = target.updateGameTimes(1, null, null,
            ZonedDateTime.parse("2023-04-07T13:00:00.000Z[Europe/London]"))
        // Guesses close should be updated..
        assertEquals(updatedGame.guessesClose, ZonedDateTime.parse("2023-04-07T13:00:00.000Z[Europe/London]"))

        // While others are not since it was null in the params
        assertEquals(createdGame.windowClose, updatedGame.windowClose)
        assertEquals(createdGame.windowStart, updatedGame.windowStart)
    }

    @DisplayName("findActiveGamesByUserId() will return list of games by user id")
    @Test
    fun canSuccessfullyFindActiveGamesByUserId() {
        val expected = Game(
            gameId = 2,
            gameName = "A second game",
            windowStart = ZonedDateTime.parse("2023-04-10T10:00:00.000Z[Europe/London]"),
            windowClose = ZonedDateTime.parse("2023-04-10T18:00:00.000Z[Europe/London]"),
            guessesClose = ZonedDateTime.parse("2023-04-10T08:00:00.000Z[Europe/London]"),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
        target.createGame(expected)

        val games: List<Game> = target.findActiveGamesByUserId("Z")

        assertEquals(games[0], createdGame)
        assertEquals(games[1], expected)
    }

    @DisplayName("finishGame() will successfully update the game deliveryTime and gameActive to false")
    @Test
    fun canSuccessfullyFinishGame() {
        val finishedGame: Game = target.finishGame(1,
            ZonedDateTime.parse("2023-04-07T16:37:00.000Z[Europe/London]"))

        assertEquals(finishedGame.deliveryTime, ZonedDateTime.parse("2023-04-07T16:37:00.000Z[Europe/London]"))
        assertEquals(finishedGame.gameActive, false)
    }

    private fun createGame(): Game {
        val gameName = "A random game name for test"

        val expected = Game(
            gameId = 1,
            gameName = gameName,
            windowStart = ZonedDateTime.parse("2023-04-07T09:00:00.000Z[Europe/London]"),
            windowClose = ZonedDateTime.parse("2023-04-07T17:00:00.000Z[Europe/London]"),
            guessesClose = ZonedDateTime.parse("2023-04-07T12:00:00.000Z[Europe/London]"),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
        target.createGame(expected)

        return expected
    }

}

