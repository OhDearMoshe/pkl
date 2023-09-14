package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Win

interface WinDao {
    @SqlUpdate("""
        INSERT INTO WIN(
            gameId,
            guessId,
            date
        )
        VALUES (
            :winningGuess.gameId,
            :winningGuess.guessId,
            :winningGuess.date
        )
        RETURNING *
    """)
    fun addWinningGuess(winningGuess: Win)
}