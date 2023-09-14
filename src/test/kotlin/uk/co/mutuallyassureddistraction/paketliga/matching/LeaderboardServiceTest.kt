package uk.co.mutuallyassureddistraction.paketliga.matching

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.PointDao
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point
import kotlin.test.assertEquals

class LeaderboardServiceTest {
    private lateinit var target: LeaderboardService

    @BeforeEach
    fun setUp() {
        val pointDao = mockk<PointDao>()
        Point(1,"Z",1,1,1,1)

        every {pointDao.getPointByUserId(any())} returns Point(2,"Y",1,1,1,3)
        every {pointDao.getPointsSortedByTotalPointsDesc()} returns arrayListOf(
            Point(2,"Y",1,1,1,3),
            Point(1,"Z",1,1,1,1)
        )

        target = LeaderboardService(pointDao)
    }

    @DisplayName("getLeaderboard() with userid will return one point")
    @Test
    fun whenGetWithUserIdReturnOnePoint() {
        val points = target.getLeaderboard("Y")
        assertEquals(points.size, 1)
        assertEquals(points[0].userId, "Y")
    }

    @DisplayName("getLeaderboard() with null userid will return all points")
    @Test
    fun whenGetWithoutUserIdReturnAllPoints() {
        val points = target.getLeaderboard(null)
        assertEquals(points.size, 2)
        assertEquals(points[0].userId, "Y")
        assertEquals(points[1].userId, "Z")
    }
}