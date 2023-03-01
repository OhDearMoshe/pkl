package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import uk.co.mutuallyassureddistraction.paketliga.dao.entity.Guess
import java.lang.Exception
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

class GuessDaoTest {
    lateinit var target: GuessDao
    val postgresContainer = PostgreSQLContainer("postgres:11.1")
        .withDatabaseName("integration-tests-db")
        .withUsername("sa")
        .withPassword("sa");
    @BeforeEach
    fun setUp() {
        postgresContainer.start()
        val jdbi = Jdbi.create(postgresContainer.createConnection("?")).apply {
            installPlugin(PostgresPlugin())
                .installPlugin(SqlObjectPlugin())
                .installPlugin(KotlinPlugin())
                .installPlugin(KotlinSqlObjectPlugin())
        }

        setUpTable(jdbi)
        target = jdbi.onDemand(GuessDao::class.java)
    }

    @AfterEach
    fun tearDown() {
        postgresContainer.stop()
    }

    @Test
    fun doesThisWork() {
        val guessId = UUID.randomUUID()
        val gameId = UUID.randomUUID()
        val expected = Guess(
            guessId = guessId,
            gameId = gameId,
            userId = "PostMasterGeneral",
            guessTime = ZonedDateTime.parse("2023-03-01T22:51:20.123330Z[Europe/London]")
        )

        target.createGuess(expected)

        val result = target.findGuessByGuessId(guessId)
        assertEquals(result, expected)
    }

    fun setUpTable(jdbi: Jdbi) {
        jdbi.withHandle<Int, Exception> {
            it.createUpdate("""
                CREATE TABLE  GUESS (
                   guessId uuid not null,
                   gameId uuid not null,
                   userId VARCHAR(50) not null,
                   guessTime VARCHAR(50) not null
                )
            """).execute()
        }
    }

}