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
import kotlin.test.Test
import kotlin.test.assertEquals

class GameUpsertServiceTest {

    private lateinit var target: GameUpsertService

    @BeforeEach
    fun setUp() {
        val gameDao = mockk<GameDao>()
        every {gameDao.createGame(any())} returns Unit
        target = GameUpsertService(gameDao)
    }

    @DisplayName("createGame() will return string with gameName and member mentioned if both values are not null")
    @Test
    fun returnStringWithNonNullGameNameAndMember() {
        val member = mockk<Member>()
        every {member.mention} returns "Z"
        val gameName = "Random Amazon package"
        val returnedString = target.createGame(gameName, "today 2pm",
            "today 7pm", "today 1pm", "1234", member, "ZLX")
        val expectedString = getExpectedString(gameName, member, "ZLX")
        assertEquals(expectedString, returnedString)
    }

    @DisplayName("createGame() will return string with default 'Game' string and username if game name and member are null")
    @Test
    fun returnStringWithNullGameNameAndMember() {
        val returnedString = target.createGame(null, "today 2pm",
            "today 7pm", "today 1pm", "1234", null, "ZLX")
        val expectedString = getExpectedString(null, null, "ZLX")
        assertEquals(returnedString, expectedString)
    }

    private fun getExpectedString(userGameName: String?, member: Member?, username: String): String {
        val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

        val startTime = LocalDateTime.now().withHourOfDay(14).withMinuteOfHour(0).toString(dtf)
        val closeTime = LocalDateTime.now().withHourOfDay(19).withMinuteOfHour(0).toString(dtf)
        val guessesCloseTime = LocalDateTime.now().withHourOfDay(13).withMinuteOfHour(0).toString(dtf)

        val gameName = userGameName ?: "Game"

        return if(member != null) {
            "$gameName by ${member.mention}" + " : package arriving between " + startTime + " and " + closeTime +
                    ". Guesses accepted until " + guessesCloseTime
        } else {
            "$gameName by $username" + " : package arriving between " + startTime + " and " + closeTime +
                    ". Guesses accepted until " + guessesCloseTime
        }
    }
}