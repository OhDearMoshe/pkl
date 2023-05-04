package uk.co.mutuallyassureddistraction.paketliga.dao.entity

import java.time.ZonedDateTime

data class Guess(
    val guessId: Int?,
    val gameId: Int,
    val userId: String,
    val guessTime: ZonedDateTime
)
