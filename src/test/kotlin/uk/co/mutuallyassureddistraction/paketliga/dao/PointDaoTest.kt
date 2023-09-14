package uk.co.mutuallyassureddistraction.paketliga.dao

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point
import kotlin.test.assertEquals

class PointDaoTest {
    private lateinit var target: PointDao
    private lateinit var testWrapper: DaoTestWrapper

    @BeforeEach
    fun setUp() {
        testWrapper = initTests()
        target = testWrapper.buildDao(PointDao::class.java)
    }

    @DisplayName("addWin() without prior insert will successfully insert a point into the table")
    @Test
    fun canSuccessfullyInsertWinIntoTable() {
        target.addWin(createdPoint())
        val result = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(result, createdPoint())
    }

    @DisplayName("addWin() with prior insert will successfully update the point and won in the table")
    @Test
    fun canSuccessfullyUpdateWinOnConflictIntoTable() {
        val expected = createdPoint()
        target.addWin(expected)
        val firstResult = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(firstResult.userId, expected.userId)
        assertEquals(firstResult.totalPoint, 1)
        assertEquals(firstResult.played, 1)
        assertEquals(firstResult.won, 1)

        target.addWin(expected)
        val secondResult = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(secondResult.userId, expected.userId)
        assertEquals(secondResult.totalPoint, 2)
        assertEquals(secondResult.played, 2)
        assertEquals(secondResult.won, 2)
    }

    @DisplayName("addLost() without prior insert will successfully insert a point into the table")
    @Test
    fun canSuccessfullyInsertLostIntoTable() {
        target.addLost(createdPoint())
        val result = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(result, createdPoint())
    }

    @DisplayName("addLost() with prior insert will successfully update the point and lost in the table")
    @Test
    fun canSuccessfullyUpdateLostOnConflictIntoTable() {
        val expected = createdPoint()
        target.addLost(expected)
        val firstResult = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(firstResult.userId, expected.userId)
        assertEquals(firstResult.totalPoint, 1)
        assertEquals(firstResult.played, 1)
        assertEquals(firstResult.lost, 1)

        target.addLost(expected)
        val secondResult = testWrapper.executeSimpleQuery<Point>(
            """SELECT * FROM POINT""".trimIndent()
        )
        assertEquals(secondResult.userId, expected.userId)
        assertEquals(secondResult.totalPoint, 1)
        assertEquals(secondResult.played, 2)
        assertEquals(secondResult.lost, 2)
    }

    @DisplayName("getPointsSortedByTotalPointsDesc() will return all points sorted descending")
    @Test
    fun canSuccessfullyGetPointsDescFromTable() {
        target.addWin(Point(1,"Z",1,1,1,1))
        target.addWin(Point(2,"Y",1,1,1,3))
        target.addWin(Point(3,"X",1,1,1,2))
        val points = target.getPointsSortedByTotalPointsDesc()
        assertEquals(points[0].userId, "Y")
        assertEquals(points[1].userId, "X")
        assertEquals(points[2].userId, "Z")
        assertEquals(points[0].totalPoint, 3)
        assertEquals(points[1].totalPoint, 2)
        assertEquals(points[2].totalPoint, 1)
    }

    @DisplayName("getPointByUserId() will return point by user id")
    @Test
    fun canSuccessfullyGetPointByUserId() {
        target.addWin(Point(1, "Z", 1, 1, 1, 1))
        val point = target.getPointByUserId("Z")
        assertEquals(point.userId, "Z")
    }

    private fun createdPoint(): Point {
        return Point(
            pointId = 1,
            userId = "Z",
            played = 1,
            won = 1,
            lost = 1,
            totalPoint = 1,
        )
    }
}