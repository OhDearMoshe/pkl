package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import uk.co.mutuallyassureddistraction.paketliga.matching.GuessUpsertService

class GuessGameExtension(private val guessUpsertService: GuessUpsertService, private val serverId: Snowflake): Extension() {
    override val name = "guessExtension"

    override suspend fun setup() {
        publicSlashCommand(::GuessGameArgs) {
            name = "guessgame"
            description = "Guess the delivery time of a game"

            guild(serverId)

            action {
                val gameId = arguments.gameid
                val guessTime = arguments.guesstime
                val userId = user.asUser().id.value.toString()

                val guessGameResponse = guessUpsertService.guessGame(gameId, guessTime, userId)

                val respondMessage = if(!guessGameResponse.success) {
                    guessGameResponse.failMessage!!
                } else {
                    "Guess created by " + user.asUser().mention +
                            " for game #" + gameId + " with time " + guessGameResponse.guessTime
                }

                respond {
                    content = respondMessage
                }
            }
        }
    }

    inner class GuessGameArgs : Arguments() {
        val gameid by int {
            name = "gameid"
            description = "Game id inputted by user"
        }
        val guesstime by string {
            name = "guesstime"
            description = "Time guessed by user for the delivery time of the game"
        }
    }
}