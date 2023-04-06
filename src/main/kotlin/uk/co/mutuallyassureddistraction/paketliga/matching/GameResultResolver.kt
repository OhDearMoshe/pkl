package uk.co.mutuallyassureddistraction.paketliga.matching

import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Game
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

@Suppress("MagicNumber")
class GameResultResolver {
    fun findWinners(game: Game, guesses: List<Guess>): List<Guess> {
        var closestGuesses = mutableListOf<Guess>()
        val deliveryTime = game.deliveryTime
        var shortestDistance = 1000000000000000000L
        guesses.forEach {
            val currentDistance = it.guessTime.until(deliveryTime, ChronoUnit.SECONDS).absoluteValue
            if(currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                closestGuesses = mutableListOf(it)
            } else if(currentDistance == shortestDistance) {
                closestGuesses.add(it)
            }
        }
        return closestGuesses
    }
}
