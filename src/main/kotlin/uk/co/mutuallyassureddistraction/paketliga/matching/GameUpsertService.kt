package uk.co.mutuallyassureddistraction.paketliga.matching

import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import dev.kord.core.entity.Member
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZoneId
import java.util.*

class GameUpsertService(private val gameDao: GameDao) {
    fun createGame(userGameName: String?, startWindow: String, closeWindow: String, guessesClose: String,
                   userId: String, member: Member?, username: String): String {
        try {
            val parser = HawkingTimeParser()
            val referenceDate = Date()
            val hawkingConfiguration = HawkingConfiguration()
            hawkingConfiguration.timeZone = ZoneId.systemDefault().toString()

            val startDates: DatesFound = parser.parse(startWindow, referenceDate, hawkingConfiguration, "eng")
            val closeDates: DatesFound = parser.parse(closeWindow, referenceDate, hawkingConfiguration, "eng")
            val guessesCloseDates: DatesFound = parser.parse(guessesClose, referenceDate, hawkingConfiguration, "eng")

            // Start or end doesn't matter if we only have one date at a time
            val startDate = startDates.parserOutputs[0].dateRange.start
            val closeDate = closeDates.parserOutputs[0].dateRange.start
            val guessesCloseDate = guessesCloseDates.parserOutputs[0].dateRange.start
            val gameName = userGameName ?: "Game"

            val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

            val gameNameString = gameNameStringMaker(gameName, member, username)
            val startDateString = startDate.toString(dtf)
            val closeDateString = closeDate.toString(dtf)
            val guessesCloseDateString = guessesCloseDate.toString(dtf)

            gameDao.createGame(
                Game(
                    gameId = null,
                    gameName = gameName,
                    windowStart = startDate.toGregorianCalendar().toZonedDateTime(),
                    windowClose = closeDate.toGregorianCalendar().toZonedDateTime(),
                    guessesClose = guessesCloseDate.toGregorianCalendar().toZonedDateTime(),
                    deliveryTime = null,
                    userId = userId,
                    gameActive = true
                )
            )

            return gameNameString + " : package arriving between " + startDateString + " and " + closeDateString +
                    ". Guesses accepted until " + guessesCloseDateString
        } catch (e: Exception) {
            // TODO logging
            return "An error has occurred, please re-check your inputs and try again"
        }
    }

    private fun gameNameStringMaker(gameName: String?, member: Member?, username: String): String {
        return if(member != null) {
            "$gameName by ${member.mention}"
        } else {
            // We need username for non-server users that are using this command, if any (hence the nullable Member)
            // Kinda unlikely, but putting this here just in case
            "$gameName by $username"
        }
    }
}