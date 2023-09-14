package uk.co.mutuallyassureddistraction.paketliga.matching

import uk.co.mutuallyassureddistraction.paketliga.dao.GameDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime

class GameFinderService(private val gameDao: GameDao) {
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
        return FindGamesResponse(
            game.gameId!!,
            game.userId,
            game.windowStart,
            game.windowClose,
            game.guessesClose
        )
    }
}

class FindGamesResponse(
    val gameId: Int,
    val userId: String,
    val windowStart: ZonedDateTime,
    val windowClose: ZonedDateTime,
    val guessesClose: ZonedDateTime
)

