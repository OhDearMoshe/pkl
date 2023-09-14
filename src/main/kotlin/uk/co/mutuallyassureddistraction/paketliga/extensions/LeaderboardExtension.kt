package uk.co.mutuallyassureddistraction.paketliga.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import uk.co.mutuallyassureddistraction.paketliga.matching.LeaderboardService

class LeaderboardExtension(private val leaderboardService: LeaderboardService, private val serverId: Snowflake): Extension() {
    override val name = "leaderboardExtension"

    override suspend fun setup() {
        publicSlashCommand(::LeaderboardArgs) {
            name = "leaderboard"
            description = "Get leaderboard"

            guild(serverId)

            action {
                val userId = arguments.userId?.asUser()?.id?.value?.toString()

                val leaderboard = leaderboardService.getLeaderboard(userId)

                if(leaderboard.isEmpty()) {
                    respondEphemeral {
                        content = "No data found"
                    }
                } else {
                    val kord = this@LeaderboardExtension.kord

                    val paginator = respondingPaginator {
                        var counter = 1
                        leaderboard.chunked(10).map { response ->
                            val pageFields = ArrayList<EmbedBuilder.Field>()
                            response.forEach {
                                val memberBehavior = MemberBehavior(serverId, Snowflake(it.userId), kord)

                                val field = EmbedBuilder.Field()
                                field.name =
                                    "# " + counter + " : " + memberBehavior.asMember().displayName +
                                            " | " + it.totalPoint + " points"
                                field.value = "Played: " + it.played + " - Won: " + it.won + " - Lost: " + it.lost
                                pageFields.add(field)
                                counter++
                            }

                            page {
                                title = "PaketLiga Leaderboard: "
                                fields = pageFields
                            }
                        }

                        // This will make the pagination function (next prev etc) to disappear after timeout time
                        timeoutSeconds = 20L
                    }

                    paginator.send()
                }
            }
        }
    }

    inner class LeaderboardArgs : Arguments() {
        val userId by optionalUser {
            name = "username"
            description = "Username inputted by the user"
        }
    }
}