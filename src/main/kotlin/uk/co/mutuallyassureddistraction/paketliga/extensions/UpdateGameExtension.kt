package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import uk.co.mutuallyassureddistraction.paketliga.matching.GameUpsertService

class UpdateGameExtension(private val gameUpsertService: GameUpsertService, private val serverId: Snowflake) : Extension() {
    override val name = "updateGameExtension"
    override suspend fun setup() {
        publicSlashCommand(::UpdateGameArgs) {
            name = "updategame"
            description = "Ask the bot to update a game of PKL"

            guild(serverId)

            action {
                respond {
                    content = "game updated"
                }
            }
        }
    }

    inner class UpdateGameArgs : Arguments() {
        val gameid by int {
            name = "gameid"
            description = "Game id inputted by user"
        }

        val startwindow by optionalString {
            name = "startwindow"
            description = "Start window time inputted by user"
        }

        val closewindow by optionalString {
            name = "closewindow"
            description = "Close window time inputted by user"
        }

        val guessesclose by optionalString {
            name = "guessesclose"
            description = "Close window time inputted by user"
        }
    }

}