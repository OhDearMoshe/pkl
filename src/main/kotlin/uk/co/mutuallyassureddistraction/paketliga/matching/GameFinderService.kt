package uk.co.mutuallyassureddistraction.paketliga.matching

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.util.*

class GameFinderService(private val gameDao: GameDao) {

    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    fun findGames(userId: String?, gameName: String?, gameId: Int?): List<FindGamesResponse> {
        val gamesResponseList = ArrayList<FindGamesResponse>()
        if(gameId != null) {
            val searchedGame: Game? = gameDao.findActiveGameById(gameId)
            if(searchedGame != null) {
                gamesResponseList.add(buildResponse(searchedGame))
            }
        } else {
            val searchedGames: List<Game> = gameDao.findActiveGames(gameName, userId)
            for(searchedGame in searchedGames) {
                gamesResponseList.add(buildResponse(searchedGame))
            }
        }

        return gamesResponseList
    }

    private fun buildResponse(game: Game): FindGamesResponse {
        val startDateString = DateTime(game.windowStart.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(game.windowStart.zone))).toString(dtf)
        val closeDateString = DateTime(game.windowClose.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(game.windowClose.zone))).toString(dtf)
        val guessesCloseDateString = DateTime(game.guessesClose.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(game.guessesClose.zone))).toString(dtf)

        return FindGamesResponse(
            game.gameId!!,
            game.gameName,
            game.userId,
            startDateString,
            closeDateString,
            guessesCloseDateString
        )
    }
}

class FindGamesResponse(
    val gameId: Int,
    val gameName: String,
    val userId: String,
    val windowStart: String,
    val windowClose: String,
    val guessesClose: String,
)

