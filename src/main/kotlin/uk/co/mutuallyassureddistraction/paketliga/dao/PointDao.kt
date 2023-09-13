package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Point

interface PointDao {
    @SqlUpdate("""
        INSERT INTO POINT as pnt(
            userId,
            played,
            won,
            lost,
            totalPoint
        )
        VALUES (
            :point.userId,
            :point.played,
            :point.won,
            :point.lost,
            :point.totalPoint
        )
        ON CONFLICT (userId) DO UPDATE
            SET 
                totalPoint = pnt.totalPoint + 1,
                played = pnt.played + 1,
                won = pnt.won + 1
            WHERE pnt.userId = :point.userId
        RETURNING *
    """)
    fun addWin(point: Point)

    @SqlUpdate("""
        INSERT INTO POINT as pnt(
            userId,
            played,
            won,
            lost,
            totalPoint
        )
        VALUES (
            :point.userId,
            :point.played,
            :point.won,
            :point.lost,
            :point.totalPoint
        )
        ON CONFLICT (userId) DO UPDATE
            SET 
                played = pnt.played + 1,
                lost = pnt.lost + 1
            WHERE pnt.userId = :point.userId
        RETURNING *
    """)
    fun addLost(point: Point)
}