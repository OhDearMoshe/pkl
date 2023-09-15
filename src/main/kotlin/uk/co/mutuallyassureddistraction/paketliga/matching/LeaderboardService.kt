package uk.co.mutuallyassureddistraction.paketliga.matching

import uk.co.mutuallyassureddistraction.paketliga.dao.PointDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point

class LeaderboardService(private val pointDao: PointDao) {
    fun getLeaderboard(userId: String?): List<Point> {
        try {
            if (userId == null) {
                return pointDao.getPointsSortedByTotalPointsDesc()
            }

            val userPoint = pointDao.getPointByUserId(userId) ?: return arrayListOf()

            return arrayListOf(userPoint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return arrayListOf()
    }
}