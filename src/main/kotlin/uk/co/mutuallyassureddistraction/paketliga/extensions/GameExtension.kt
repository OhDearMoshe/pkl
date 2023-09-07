package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.env
import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import uk.co.mutuallyassureddistraction.paketliga.matching.GameUpsertService
import java.time.ZoneId
import java.util.*


val SERVER_ID = Snowflake(
    env("SERVER_ID").toLong()  // Get the test server ID from the env vars or a .env file
)

class GameExtension : Extension() {
    override val name = "gameExtension"

    override suspend fun setup() {
        publicSlashCommand(::PaketGameArgs) {  // Public slash commands have public responses
            name = "paketliga"
            description = "Ask the bot to create a game of PKL"

            // Use guild commands for testing, global ones take up to an hour to update
            guild(SERVER_ID)

            action {
                val kord = this@GameExtension.kord
                val gameUpsertService = GameUpsertService()
                val createGameResponse = gameUpsertService.createGame(
                    arguments.gamename, arguments.startwindow, arguments.closewindow, arguments.guessesclose,
                    user.asUser().id.value.toString(), member?.asMember(), user.asUser().username
                )

                respond {
                    content = createGameResponse
                }

                //TODO put the logic in try/catch and add logging?
            }
        }
    }

    /**
     * Arguments for the game, basically for this extension.
     * Planned arguments are similar to Game Entity:
     * from user input: startWindow, closeWindow, guessesClose, gameName (optional)
     * from impl: userId
     * We're not using camelCase because currently it doesn't work (counted as coalesced string)
     */
    inner class PaketGameArgs : Arguments() {
        val startwindow by string {
            name = "startwindow"
            description = "Start window time inputted by user"
        }

        val closewindow by string {
            name = "closewindow"
            description = "Close window time inputted by user"
        }

        val guessesclose by string {
            name = "guessesclose"
            description = "Close window time inputted by user"
        }

        val gamename by optionalString {
            name = "gamename"
            description = "Game name inputted by user"
        }
    }
}
