package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.utils.respond

class SlapExtension : Extension() {
    override val name = "slapExtension"

    override suspend fun setup() {
        chatCommand {
            name = "slap"
            description = "Get slapped!"

            action {
                message.respond(
                    "*slaps you with a large, smelly trout!*"
                )
            }
        }
    }
}
