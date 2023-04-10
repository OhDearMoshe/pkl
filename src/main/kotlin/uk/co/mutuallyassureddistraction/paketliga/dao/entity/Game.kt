package uk.co.mutuallyassureddistraction.paketliga.dao.entity

import java.time.ZonedDateTime
import java.util.UUID

data class Game(
    val gameId: UUID,
    val gameName: String,
    val windowStart: ZonedDateTime,
    val windowClose: ZonedDateTime,
    val guessesClose: ZonedDateTime,
    val deliveryTime: ZonedDateTime?,
    val userId: String,
    val gameActive: Boolean
)
