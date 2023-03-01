package uk.co.mutuallyassureddistraction.paketliga.dao

import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess

interface GuessDao {
    fun createGuess(guess: Guess)
}