package uk.co.mutuallyassureddistraction.paketliga.matching

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.mutuallyassureddistraction.paketliga.dao.GuessDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.util.*

class GuessFinderService(private val guessDao: GuessDao) {

    private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy HH:mm")

    fun findGuesses(gameId: Int?, guessId: Int?): List<FindGuessesResponse> {
        val guessResponseList = ArrayList<FindGuessesResponse>()
        if(gameId == null && guessId == null) {
            return guessResponseList
        }

        if(gameId != null) {
            val searchedGuesses: List<Guess> = guessDao.findGuessesByGameId(gameId)
            for(searchedGuess in searchedGuesses) {
                guessResponseList.add(buildResponse(searchedGuess))
            }
        } else {
            val searchedGuess: Guess = guessDao.findGuessByGuessId(guessId!!)
            guessResponseList.add(buildResponse(searchedGuess))
        }

        return guessResponseList
    }

    private fun buildResponse(guess: Guess): FindGuessesResponse {
        val guessTimeString = DateTime(guess.guessTime.toInstant().toEpochMilli(),
            DateTimeZone.forTimeZone(TimeZone.getTimeZone(guess.guessTime.zone))).toString(dtf)

        return FindGuessesResponse(guess.guessId!!, guess.gameId, guess.userId, guessTimeString)
    }

    class FindGuessesResponse(
        val guessId: Int,
        val gameId: Int,
        val userId: String,
        val guessTime: String,
    )
}


