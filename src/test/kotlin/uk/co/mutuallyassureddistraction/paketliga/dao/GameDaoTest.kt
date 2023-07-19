package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.postgresql.util.PSQLException
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
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

    @DisplayName("finishGame() will successfully update the game deliveryTime and gameActive to false")
    @Test
    fun canSuccessfullyFinishGame() {
        val finishedGame: Game = target.finishGame(1,
            ZonedDateTime.parse("2023-04-07T16:37:00.000Z[Europe/London]"))

        assertEquals(finishedGame.deliveryTime, ZonedDateTime.parse("2023-04-07T16:37:00.000Z[Europe/London]"))
        assertEquals(finishedGame.gameActive, false)
    }

    @DisplayName("finishGame() will failed to update the game if delivery time is outside the window")
    @Test
    fun failToFinishGameWhenDeliveryTimeNotInRange() {

        try {
            target.finishGame(
                1,
                ZonedDateTime.parse("2023-04-07T20:37:00.000Z[Europe/London]")
            )
        } catch(e: UnableToExecuteStatementException) {
            val original = e.cause
            assertIs<PSQLException>(original)
            assertEquals("ERRG0", original.sqlState)
        }
    }

    @DisplayName("findActiveGames() with name will successfully return a game by name")
    @Test
    fun canSuccessfullyFindActiveGameByName() {
        val searchedGame: List<Game> = target.findActiveGames("random", null)
        assertEquals(createdGame, searchedGame[0])
    }

    @DisplayName("findActiveGame() with null params will successfully get all active games")
    @Test
    fun canSuccessfullyFindActiveGames() {
        val secondCreatedGame = Game(
            gameId = 2,
            gameName = "A second game",
            windowStart = ZonedDateTime.parse("2023-04-10T10:00:00.000Z[Europe/London]"),
            windowClose = ZonedDateTime.parse("2023-04-10T18:00:00.000Z[Europe/London]"),
            guessesClose = ZonedDateTime.parse("2023-04-10T08:00:00.000Z[Europe/London]"),
            deliveryTime = null,
            userId = "Z",
            gameActive = true
        )
        target.createGame(secondCreatedGame)

        val games: List<Game> = target.findActiveGames(null, null)

        assertEquals(createdGame, games[0])
        assertEquals(secondCreatedGame, games[1])
    }


    @DisplayName("findActiveGames() with userId param will return list of games by user id")
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

        val games: List<Game> = target.findActiveGames(null, "Z")

        assertEquals(games[0], createdGame)
        assertEquals(games[1], expected)
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

