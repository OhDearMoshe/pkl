package uk.co.mutuallyassureddistraction.paketliga.matching

import dev.kord.core.entity.Member
import io.mockk.every
import io.mockk.mockk
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class GameUpsertServiceTest {

    private lateinit var target: GameUpsertService
    private lateinit var dtf: DateTimeFormatter

    @BeforeEach
    fun setUp() {
        val gameDao = mockk<GameDao>()
        val guessFinderService = mockk<GuessFinderService>()
        every {gameDao.createGame(any())} returns getGameStub()
        every {gameDao.findActiveGameById(999)} returns null
        every {gameDao.findActiveGameById(1)} returns mockk<Game>()
        every {gameDao.updateGameTimes(any(), any(), any(), any())} returns getUpdatedGameStub()

        val guessesResponse = arrayListOf(
            GuessFinderService.FindGuessesResponse (
                guessId = 1,
                gameId = 1,
                userId = "Z",
                guessTime = "\"2023-04-10T10:00:00.000Z[Europe/London]"
            )
        )
        every {guessFinderService.findGuesses(any(), any())} returns guessesResponse

        target = GameUpsertService(gameDao, guessFinderService)
        dtf = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")
    }

    @DisplayName("createGame() will return string with gameName and member mentioned if both values are not null")
    @Test
    fun returnStringWithNonNullGameNameAndMember() {
        val member = mockk<Member>()
        every {member.mention} returns "Z"
        val gameName = "Random Amazon package"
        val returnedString = target.createGame(gameName, "today 2pm",
            "today 7pm", "today 1pm", "1234", member, "ZLX")
        val expectedString = getCreateGameExpectedString(gameName, member, "ZLX")
        assertEquals(expectedString, returnedString)
    }

    @DisplayName("createGame() will return string with default 'Game' string and username if game name and member are null")
    @Test
    fun returnStringWithNullGameNameAndMember() {
        val returnedString = target.createGame(null, "today 2pm",
            "today 7pm", "today 1pm", "1234", null, "ZLX")
        val expectedString = getCreateGameExpectedString(null, null, "ZLX")
        assertEquals(returnedString, expectedString)
    }

    @DisplayName("updateGame() will return wrong Game ID string")
    @Test
    fun returnStringWithWrongGameIDInformation() {
        target.createGame(null, "today 2pm",
            "today 7pm", "today 1pm", "1234", null, "ZLX")
        val (updateString, _) = target.updateGame(999, null, null, null)
        val expectedString = "Wrong Game ID, please check your gameId input and try again"
        assertEquals(updateString[0], expectedString)
    }

    @DisplayName("updateGame() will return updated game string")
    @Test
    fun returnStringWithUpdatedGameInfo() {
        target.createGame(null, "today 2pm",
            "today 7pm", "today 1pm", "1234", null, "ZLX")
        val (updateString, _) = target.updateGame(1, "today 3 pm", null, "today 2 pm")

        val startTime = LocalDateTime.now().withHourOfDay(15).withMinuteOfHour(0).toString(dtf)
        val closeTime = LocalDateTime.now().withHourOfDay(19).withMinuteOfHour(0).toString(dtf)
        val guessesCloseTime = LocalDateTime.now().withHourOfDay(14).withMinuteOfHour(0).toString(dtf)
        val expectedString = "Game #1 updated: package now arriving between " + startTime +
                " and " + closeTime + ". Guesses accepted until " + guessesCloseTime
        assertEquals(updateString[0], expectedString)
    }

    @DisplayName("updateGame() will return userIds")
    @Test
    fun returnStringWithUserIds() {
        target.createGame(null, "today 2pm",
            "today 7pm", "today 1pm", "1234", null, "ZLX")
        val (_, userIds) = target.updateGame(1, "today 3 pm", null, "today 2 pm")

        assertEquals(userIds[0], "Z")
    }

    private fun getUpdatedGameStub(): Game {
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

    private fun getCreateGameExpectedString(userGameName: String?, member: Member?, username: String): String {
        val startTime = LocalDateTime.now().withHourOfDay(14).withMinuteOfHour(0).toString(dtf)
        val closeTime = LocalDateTime.now().withHourOfDay(19).withMinuteOfHour(0).toString(dtf)
        val guessesCloseTime = LocalDateTime.now().withHourOfDay(13).withMinuteOfHour(0).toString(dtf)

        val gameName = userGameName ?: "Game"

        return if(member != null) {
            "$gameName (#1) by ${member.mention}" + " : package arriving between " + startTime + " and " + closeTime +
                    ". Guesses accepted until " + guessesCloseTime
        } else {
            "$gameName (#1) by $username" + " : package arriving between " + startTime + " and " + closeTime +
                    ". Guesses accepted until " + guessesCloseTime
        }
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