package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game

interface GameDao {
     @SqlUpdate("""
          INSERT INTO GAME(
               gameName,
               windowStart,
               windowClose,
               guessesClose,
               deliveryTime,
               userId,
               gameActive
          )
          VALUES (
               :game.gameName,
               :game.windowStart,
               :game.windowClose,
               :game.guessesClose,
               :game.deliveryTime,
               :game.userId,
               :game.gameActive
          )
     """)
     fun createGame(game: Game)

     @SqlQuery("""
          SELECT * FROM GAME
          WHERE gameName LIKE concat('%',:gameName,'%')
          AND gameActive = 'TRUE'
     """)
     fun findActiveGameByName(gameName: String): Game

}
