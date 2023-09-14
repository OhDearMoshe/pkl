package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GuessFinderServiceTest {
    private lateinit var target: GuessFinderService
    private val expectedGuess: Guess = getGuessStub()
    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    @BeforeEach
    fun setUp() {
        val guessDao = mockk<GuessDao>()
        every {guessDao.findGuessByGuessId(any())} returns expectedGuess
        every {guessDao.findGuessesByGameId(any())} returns listOf(expectedGuess)
        target = GuessFinderService(guessDao)
    }

    @DisplayName("findGuesses() with gameId param will return list of guesses after searched using gameId")
    @Test
    fun returnListOfResponseWhenSearchingWithGameId() {
        val returnedList = target.findGuesses(1, null)
        assertEquals(returnedList[0].gameId, expectedGuess.gameId)
        assertEquals(returnedList[0].userId, expectedGuess.userId)
        assertEquals(returnedList[0].guessTime, zonedDateTimeToString(expectedGuess.guessTime))
    }

    @DisplayName("findGuesses() with guessId param will return searched guess")
    @Test
    fun returnListOfResponseWhenSearchingWithGuessId() {
        val returnedList = target.findGuesses(null, 1)
        assertEquals(returnedList[0].gameId, expectedGuess.gameId)
        assertEquals(returnedList[0].userId, expectedGuess.userId)
        assertEquals(returnedList[0].guessTime, zonedDateTimeToString(expectedGuess.guessTime))
    }

    private fun zonedDateTimeToString(zdt : ZonedDateTime): String {
        return DateTime(zdt.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(zdt.zone))).toString(dtf)
    }

    private fun getGuessStub(): Guess {
        return Guess (
            guessId = 1,
            gameId = 1,
            userId = "Z",
            guessTime = ZonedDateTime.now().withHour(14).withMinute(38)
        )
    }
}