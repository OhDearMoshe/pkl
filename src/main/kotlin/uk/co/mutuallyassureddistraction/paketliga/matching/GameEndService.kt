package uk.co.mutuallyassureddistraction.paketliga.matching

import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.PointDao
import uk.co.mutuallyassureddistraction.paketliga.dao.WinDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Win
import java.sql.SQLException
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class GameEndService(
    private val guessDao: GuessDao,
    private val gameDao: GameDao,
    private val pointDao: PointDao,
    private val winDao: WinDao,
    private val gameResultResolver: GameResultResolver) {

    private val parser = HawkingTimeParser()
    private val referenceDate = Date()
    private val hawkingConfiguration = HawkingConfiguration()

    fun endGame(gameId: Int, actualTime: String): Pair<String?, List<Guess>> {
        var searchedGame: Game = gameDao.findActiveGameById(gameId) ?: return Pair("No games found.", arrayListOf())
        val deliveryDateTimes: DatesFound = parseDate(actualTime)
        val deliveryDateTime = deliveryDateTimes.parserOutputs[0].dateRange.start
        val deliveryDateTimeInstant = Instant.ofEpochMilli(deliveryDateTime.millis)
        val zonedDeliveryDateTime = ZonedDateTime.ofInstant(deliveryDateTimeInstant, searchedGame.windowStart.zone)
        // 1. we finish the game
        try {
            searchedGame = gameDao.finishGame(gameId, zonedDeliveryDateTime)
        } catch (e: Exception) {
            var errorString = "An error has occurred, please re-check your inputs and try again"
            when (e) {
                is UnableToExecuteStatementException -> {
                    val original = e.cause as SQLException
                    when (original.sqlState) {
                        "ERRG0" -> {
                            errorString =
                                "Ending game failed, delivery time #$zonedDeliveryDateTime is not between start and closing window of game #$gameId"
                        }
                    }
                }

                else -> {
                    e.printStackTrace()
                }
            }
            return Pair(errorString, arrayListOf())
        }

        // 2. we get the guesses and find the winning guess(es)
        val guesses = guessDao.findGuessesByGameId(gameId)
        val winningGuesses = gameResultResolver.findWinners(searchedGame, guesses)
        val losingGuesses = ArrayList<Guess>()

        // 3. find losing guesses and update their lost count
        guesses.forEach {
            if (!winningGuesses.contains(it)) {
                losingGuesses.add(it)
                pointDao.addLost(
                    Point(
                        pointId = null,
                        userId = it.userId,
                        played = 1,
                        won = 0,
                        lost = 1,
                        totalPoint = 0
                    )
                )
            }
        }

        // 4. for each winning guesses, add winning guess, win and point to their count
        winningGuesses.forEach {
            winDao.addWinningGuess(
                Win(
                    winId = null,
                    gameId = it.gameId,
                    guessId = it.guessId!!,
                    date = ZonedDateTime.now()
                )
            )
            pointDao.addWin(
                Point(
                    pointId = null,
                    userId = it.userId,
                    played = 1,
                    won = 1,
                    lost = 0,
                    totalPoint = 1
                )
            )
        }

        return Pair(null, winningGuesses)
    }

    private fun parseDate(dateString: String): DatesFound {
        return parser.parse(dateString, referenceDate, hawkingConfiguration, "eng")
    }
}
