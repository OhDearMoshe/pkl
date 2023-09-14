package uk.co.mutuallyassureddistraction.paketliga.matching

import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.sql.SQLException
import java.util.*

class GuessUpsertService(private val guessDao: GuessDao) {

    private val parser = HawkingTimeParser()
    private val referenceDate = Date()
    private val hawkingConfiguration = HawkingConfiguration()
    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    fun guessGame(gameId: Int, guessTime: String, userId: String): GuessGameResponse {
        val guessDates: DatesFound = parseDate(guessTime)
        val guessDate = guessDates.parserOutputs[0].dateRange.start
        val guessDateString = guessDate.toString(dtf)

        try {
            guessDao.createGuess(
                Guess(
                    guessId = null,
                    gameId = gameId,
                    guessTime = guessDate.toGregorianCalendar().toZonedDateTime(),
                    userId = userId
                )
            )

            return GuessGameResponse(true, null, gameId, userId, guessDateString)
        } catch (e: Exception) {
            var errorString = "An error has occurred, please re-check your inputs and try again"
            // TODO logging
            when(e) {
                is UnableToExecuteStatementException -> {
                    val original = e.cause as SQLException
                    when (original.sqlState) {
                        "23505" -> {
                            errorString = "Guessing failed, there is already a guess with time $guessTime"
                        }
                        "ERRA0" -> {
                            errorString = "Guessing failed, there is no active game with game ID #$gameId"
                        }
                        "ERRA1" -> {
                            errorString = "Guessing failed, guess time is not between start and closing window of game #$gameId"
                        }
                    }
                }
                else -> {
                    e.printStackTrace()
                }
            }

            return GuessGameResponse(false, errorString, gameId, userId, guessTime)
        }
    }

    class GuessGameResponse(
        val success: Boolean,
        val failMessage: String?,
        val gameId: Int,
        val userId: String,
        val guessTime: String,
    )

    private fun parseDate(dateString: String): DatesFound {
        return parser.parse(dateString, referenceDate, hawkingConfiguration, "eng")
    }
}