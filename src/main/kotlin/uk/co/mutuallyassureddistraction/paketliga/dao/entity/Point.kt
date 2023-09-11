package uk.co.mutuallyassureddistraction.paketliga.dao.entity

data class Point(
    val pointId: Int?,
    val userId: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val totalPoint: Int,
)