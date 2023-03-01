package uk.co.mutuallyassureddistraction.paketliga.dao.entity

import java.time.ZonedDateTime
import java.util.UUID

data class Guess(
    val guessId: UUID,
    val gameId: UUID,
    val userId: String,
    val guessTime: ZonedDateTime

)
