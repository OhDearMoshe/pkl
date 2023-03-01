package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.postgres.UUIDArgumentFactory
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.kotlin.BindKotlin
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.util.*

interface GuessDao {
    @SqlUpdate(INSERT_GUESS)
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
    fun findGuessByGuessId(@Bind("id") guessId: UUID): Guess

    companion object {
        private const val INSERT_GUESS = """
            INSERT INTO GUESS(
                guessId,
                gameId,
                userId,
                guessTime
            )
            VALUES (
                :guess.guessId,
                :guess.gameId,
                :guess.userId,
                :guess.guessTime
            )
        """
    }
}