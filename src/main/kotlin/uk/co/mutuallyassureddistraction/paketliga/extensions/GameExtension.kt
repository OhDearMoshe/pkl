package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.env
import com.zoho.hawking.HawkingTimeParser
import com.zoho.hawking.datetimeparser.configuration.HawkingConfiguration
import com.zoho.hawking.language.english.model.DatesFound
import dev.kord.common.entity.Snowflake
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
                val parser = HawkingTimeParser()
                val referenceDate = Date()
                val hawkingConfiguration = HawkingConfiguration()
                hawkingConfiguration.timeZone = ZoneId.systemDefault().toString()

                val startDates : DatesFound = parser.parse(arguments.startwindow, referenceDate, hawkingConfiguration, "eng")
                val closeDates : DatesFound = parser.parse(arguments.closewindow, referenceDate, hawkingConfiguration, "eng")

                // Start or end doesn't matter if we only have one date at a time
                val startDate = startDates.parserOutputs[0].dateRange.start
                val closeDate = closeDates.parserOutputs[0].dateRange.start

                respond {
                    content = "Okay, you've set up your time window from ${startDate.toString()} to ${closeDate.toString()}"
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
    }
}
