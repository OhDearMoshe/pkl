package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Snowflake

class CommandsExtension(private val serverId: Snowflake): Extension() {
    override val name = "helpExtension"

    override suspend fun setup() {
        chatCommand() {
            name = "commands"
            description = "Get help with commands"

            action {
                message.respond {
                    content = """
                        **List of commands:**
                        `/paketliga` - Create a PKL game 
                        `/findgames` - Find **active** game(s)
                        `/guessgame` - Guess an active game
                        `/findguess` - Find guess(es) from game(s)
                        `/updategame` - Update an active game
                        `/endgame` - End an active game
                        `/leaderboard` - Show leaderboard sorted by total points descending
                    """.trimIndent()
                }
            }

        }
    }
}