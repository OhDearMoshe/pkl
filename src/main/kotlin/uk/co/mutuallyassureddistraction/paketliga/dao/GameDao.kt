package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import java.time.ZonedDateTime

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
          UPDATE GAME
          SET 
               windowStart = COALESCE(:windowStart, windowStart),
               windowClose = COALESCE(:windowClose, windowClose),
               guessesClose = COALESCE(:guessesClose, windowClose)
          WHERE gameId = :id
          RETURNING *
     """)
     fun updateGameTimes(@Bind("id")gameId: Int, @Bind("windowStart")windowStart: ZonedDateTime?,
                         @Bind("windowClose")windowClose: ZonedDateTime?,
                         @Bind("guessesClose")guessesClose: ZonedDateTime?): Game

     @SqlQuery("""
          UPDATE Game
          SET
               deliveryTime = :deliveryTime,
               gameActive = 'FALSE'
          WHERE gameId = :id
          RETURNING *
     """)
     fun finishGame(@Bind("id")gameId: Int, @Bind("deliveryTime")deliveryTime: ZonedDateTime): Game

     @SqlQuery("""
          SELECT * FROM GAME
          WHERE gameName LIKE concat('%',:gameName,'%')
          AND gameActive = 'TRUE'
     """)
     fun findActiveGameByName(gameName: String): Game

     @SqlQuery("""
          SELECT * FROM GAME
          WHERE gameId = :id
          AND gameActive = 'TRUE'
     """)
     fun findActiveGameById(@Bind("id")gameId: Int): Game?

     @SqlQuery("""
          SELECT * FROM GAME
          WHERE userId = :id
          AND gameActive = 'TRUE'
     """)
     fun findActiveGamesByUserId(@Bind("id")useId: String): List<Game>
}
