package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GameFinderServiceTest {
    private lateinit var target: GameFinderService
    private val expectedGame: Game = getGameStub()
    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    @BeforeEach
    fun setUp() {
        val gameDao = mockk<GameDao>()
        every {gameDao.findActiveGameById(any())} returns expectedGame
        every {gameDao.findActiveGames(any(), any()) } returns listOf(expectedGame)
        target = GameFinderService(gameDao)
    }

    @DisplayName("findGames() with gameId param will return a game after searched using gameId")
    @Test
    fun returnListOfResponseWhenSearchingWithGameId() {
        val returnedList = target.findGames(null, null, 1)
        assertEquals(returnedList[0].gameId, expectedGame.gameId)
        assertEquals(returnedList[0].userId, expectedGame.userId)
        assertEquals(returnedList[0].windowStart, zonedDateTimeToString(expectedGame.windowStart))
        assertEquals(returnedList[0].windowClose, zonedDateTimeToString(expectedGame.windowClose))
        assertEquals(returnedList[0].guessesClose, zonedDateTimeToString(expectedGame.guessesClose))
    }

    @DisplayName("findGames() with gameName / userId param will return searched game")
    @Test
    fun returnListOfResponseWhenSearchingWithGameNameAndOrUserId() {
        val returnedList = target.findGames("Z", "testing", null)
        assertEquals(returnedList[0].gameId, expectedGame.gameId)
        assertEquals(returnedList[0].userId, expectedGame.userId)
        assertEquals(returnedList[0].windowStart, zonedDateTimeToString(expectedGame.windowStart))
        assertEquals(returnedList[0].windowClose, zonedDateTimeToString(expectedGame.windowClose))
        assertEquals(returnedList[0].guessesClose,zonedDateTimeToString(expectedGame.guessesClose))
    }

    private fun zonedDateTimeToString(dtz : ZonedDateTime): String {
        return DateTime(dtz.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(dtz.zone))).toString(dtf)
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