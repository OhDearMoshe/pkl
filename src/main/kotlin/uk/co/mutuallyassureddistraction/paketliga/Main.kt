package uk.co.mutuallyassureddistraction.paketliga

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import uk.co.mutuallyassureddistraction.paketliga.extensions.GameExtension
import uk.co.mutuallyassureddistraction.paketliga.extensions.SlapExtension

val SERVER_ID = Snowflake(
    env("SERVER_ID").toLong()  // Get the test server ID from the env vars or a .env file
)
private val BOT_TOKEN = env("BOT_TOKEN") // Get the bot' token from the env vars or a .env file

suspend fun main() {
    val bot = ExtensibleBot(BOT_TOKEN) {
        chatCommands {
            enabled = true
            prefix { _ -> "?" }
        }

        extensions {
//            add(::SlapExtension)
            add(::GameExtension)
        }
    }

    bot.start()
}
