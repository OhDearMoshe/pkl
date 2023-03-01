package uk.co.mutuallyassureddistraction.paketliga.dao

import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game

interface GameDao {
     fun createGame(game: Game)
     fun findActiveGameByName(gameName: String): Game
}