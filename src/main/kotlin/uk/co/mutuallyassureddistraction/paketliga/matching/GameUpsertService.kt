package uk.co.mutuallyassureddistraction.paketliga.matching

import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import dev.kord.core.entity.Member
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZoneId
import java.util.*

class GameUpsertService(private val gameDao: GameDao, private val guessFinderService: GuessFinderService) {

    private val parser = HawkingTimeParser()
    private val referenceDate = Date()
    private val hawkingConfiguration = HawkingConfiguration()
    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    init {
        hawkingConfiguration.timeZone = ZoneId.systemDefault().toString()
    }

    fun createGame(userGameName: String?, startWindow: String, closeWindow: String, guessesClose: String,
                   userId: String, member: Member?, username: String): String {
        try {
            val startDates: DatesFound = parseDate(startWindow)
            val closeDates: DatesFound = parseDate(closeWindow)
            val guessesCloseDates: DatesFound = parseDate(guessesClose)

            // Start or end doesn't matter if we only have one date at a time
            val startDate = startDates.parserOutputs[0].dateRange.start
            val closeDate = closeDates.parserOutputs[0].dateRange.start
            val guessesCloseDate = guessesCloseDates.parserOutputs[0].dateRange.start
            val gameName = userGameName ?: "Game"

            val startDateString = startDate.toString(dtf)
            val closeDateString = closeDate.toString(dtf)
            val guessesCloseDateString = guessesCloseDate.toString(dtf)

            val createdGame = gameDao.createGame(
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

            val gameNameString = gameNameStringMaker(gameName, member, username, createdGame.gameId!!)

            return gameNameString + " : package arriving between " + startDateString + " and " + closeDateString +
                    ". Guesses accepted until " + guessesCloseDateString
        } catch (e: Exception) {
            // TODO logging
            e.printStackTrace()
            return "An error has occurred, please re-check your inputs and try again"
        }
    }

    fun updateGame(gameId: Int, startWindow: String?, closeWindow: String?, guessesClose: String?): Pair<Array<String>, List<String>> {
        try {
            gameDao.findActiveGameById(gameId)
                ?: return Pair(
                    arrayOf("Wrong Game ID, please check your gameId input and try again"),
                    arrayListOf(""))

            val startDates: DatesFound? = startWindow?.let { parseDate(startWindow) }
            val closeDates: DatesFound? = closeWindow?.let { parseDate(closeWindow) }
            val guessesCloseDates: DatesFound? = guessesClose?.let { parseDate(guessesClose) }

            val startDate = startDates?.let { startDates.parserOutputs[0].dateRange.start }
            val closeDate = closeDates?.let { closeDates.parserOutputs[0].dateRange.start }
            val guessesCloseDate = guessesCloseDates?.let { guessesCloseDates.parserOutputs[0].dateRange.start }

            val updatedGame: Game = gameDao.updateGameTimes(gameId,
                startDate?.let { startDate.toGregorianCalendar().toZonedDateTime() },
                closeDate?.let { closeDate.toGregorianCalendar().toZonedDateTime() },
                guessesCloseDate?.let { guessesCloseDate.toGregorianCalendar().toZonedDateTime() })

            val startDateString = DateTime(updatedGame.windowStart.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(updatedGame.windowStart.zone))).toString(dtf)
            val closeDateString = DateTime(updatedGame.windowClose.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(updatedGame.windowClose.zone))).toString(dtf)
            val guessesCloseDateString = DateTime(updatedGame.guessesClose.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(updatedGame.guessesClose.zone))).toString(dtf)

            val gameUpdatedString: String = "Game #" + gameId + " updated: package now arriving between " + startDateString +
                    " and " + closeDateString + ". Guesses accepted until " + guessesCloseDateString

            val guesses = guessFinderService.findGuesses(gameId, null)
            val userIds = ArrayList<String>()
            guesses.forEach {
                userIds.add(it.userId)
            }

            return Pair(arrayOf(gameUpdatedString), userIds)
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO logging
            return Pair(arrayOf("An error has occurred, please re-check your inputs and try again"), arrayListOf())
        }
    }

    private fun parseDate(dateString: String): DatesFound {
        return parser.parse(dateString, referenceDate, hawkingConfiguration, "eng")
    }

    private fun gameNameStringMaker(gameName: String?, member: Member?, username: String, gameId: Int): String {
        return if(member != null) {
            "$gameName (#$gameId) by ${member.mention}"
        } else {
            // We need username for non-server users that are using this command, if any (hence the nullable Member)
            // Kinda unlikely, but putting this here just in case
            "$gameName (#$gameId) by $username"
        }
    }
}