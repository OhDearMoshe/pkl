package uk.co.mutuallyassureddistraction.paketliga.matching

import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.PointDao
import uk.co.mutuallyassureddistraction.paketliga.dao.WinDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Win
import java.time.ZonedDateTime
import java.util.*

@Suppress("EmptyClassBlock")
class GameEndService(
    private val guessDao: GuessDao,
    private val gameDao: GameDao,
    private val pointDao: PointDao,
    private val winDao: WinDao,
    private val gameResultResolver: GameResultResolver) {

    private val parser = HawkingTimeParser()
    private val referenceDate = Date()
    private val hawkingConfiguration = HawkingConfiguration()

    fun endGame(gameId: Int, actualTime: String): Pair<String?, List<String>> {
        val searchedGame: Game = gameDao.findActiveGameById(gameId) ?: return Pair("No games found.", arrayListOf())
        val deliveryDateTimes: DatesFound = parseDate(actualTime)
        val deliveryDateTime = deliveryDateTimes.parserOutputs[0].dateRange.start

        // 1. we finish the game
        gameDao.finishGame(gameId, deliveryDateTime.toGregorianCalendar().toZonedDateTime())

        // 2. we get the guesses and find the winning guess(es)
        val guesses = guessDao.findGuessesByGameId(gameId)
        val winningGuesses = gameResultResolver.findWinners(searchedGame, guesses)
        val losingGuesses = ArrayList<Guess>()

        // 3. find losing guesses and update their lost count
        guesses.forEach {
            if(!winningGuesses.contains(it)) {
                losingGuesses.add(it)
                pointDao.addLost(Point(
                    pointId = null,
                    userId = it.userId,
                    played = 1,
                    won = 0,
                    lost = 1,
                    totalPoint = 0)
                )
            }
        }

        // 4. for each winning guesses, add winning guess, win and point to their count
        val winners = ArrayList<String>()
        winningGuesses.forEach {
            winners.add(it.userId)
            winDao.addWinningGuess(Win(
                winId = null,
                gameId = it.gameId,
                guessId = it.guessId!!,
                date = ZonedDateTime.now())
            )
            pointDao.addWin(Point(
                pointId = null,
                userId = it.userId,
                played = 1,
                won = 1,
                lost = 0,
                totalPoint = 1)
            )
        }

        return Pair(null, winners)
    }

    private fun parseDate(dateString: String): DatesFound {
        return parser.parse(dateString, referenceDate, hawkingConfiguration, "eng")
    }

}
