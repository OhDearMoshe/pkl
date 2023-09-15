package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.util.*

interface GuessDao {
    @SqlUpdate("""
            INSERT INTO GUESS as gss(
                gameId,
                userId,
                guessTime
            )
            VALUES (
                :guess.gameId,
                :guess.userId,
                :guess.guessTime
            ) 
            ON CONFLICT ON CONSTRAINT game_and_user_id DO UPDATE
                SET 
                    guessTime = :guess.guessTime
                WHERE gss.userId = :guess.userId
        """)
    fun createGuess(guess: Guess)

    @SqlQuery("""
        SELECT 
                guessId,
                gameId,
                userId,
                guessTime
                FROM GUESS
                WHERE guessId = :id
    """)
    fun findGuessByGuessId(@Bind("id") guessId: Int): Guess

    @SqlQuery("""
        SELECT * FROM GUESS
        WHERE gameId = :id
    """)
    fun findGuessesByGameId(@Bind("id") gameId: Int): List<Guess>
}
