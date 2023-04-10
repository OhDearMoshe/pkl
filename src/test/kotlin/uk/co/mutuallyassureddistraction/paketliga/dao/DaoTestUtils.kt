package uk.co.mutuallyassureddistraction.paketliga.dao

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.statement.Batch
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer


fun initTests(): DaoTestWrapper {
    val postgresContainer = PostgreSQLContainer("postgres:11.1")
        .withDatabaseName("integration-tests-db")
        .withUsername("sa")
        .withPassword("sa")

    postgresContainer.start()
    val jdbi = Jdbi.create(postgresContainer.createConnection("?")).apply {
        installPlugin(PostgresPlugin())
            .installPlugin(SqlObjectPlugin())
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
    }
    setUpDatabaseTables(jdbi)
    return DaoTestWrapper(jdbi, postgresContainer as PostgreSQLContainer<Nothing>)
}

/**
 * Final Structure and database itself is pretty undefined, so for
 * now simple and basic only
 */
fun setUpDatabaseTables(jdbi: Jdbi) {
    jdbi.withHandle<IntArray, Exception> {
        val batch: Batch = it.createBatch()
        batch.add("""
            CREATE TABLE GUESS (
               guessId uuid not null,
               gameId uuid not null,
               userId VARCHAR(50) not null,
               guessTime VARCHAR(50) not null
            )
        """.trimIndent())
        batch.add("""
            CREATE TABLE GAME (
                gameId uuid not null,
                gameName VARCHAR(50) not null,
                windowStart VARCHAR(50) not null,
                windowClose VARCHAR(50) not null,
                guessesClose VARCHAR(50) not null,
                deliveryTime VARCHAR(50) null,
                userId VARCHAR(50) not null,
                gameActive BOOLEAN not null
            )
        """.trimIndent())
        batch.execute()
    }
}

/**
 * Test wrapper can wrap a few basic functions
 */
class DaoTestWrapper(val jdbi: Jdbi,
                     val container: JdbcDatabaseContainer<Nothing>
) {

    fun executeSimpleUpdate(simpleUpdate: String) {
        jdbi.withHandle<Int, Exception> {
            it.createUpdate(simpleUpdate).execute()
        }
    }

    inline fun <reified T> executeSimpleQuery(simpleQuery: String): T {
        return jdbi.withHandle<T, Exception> {
            it.createQuery(simpleQuery)
                .mapTo(T::class.java)
                .first()
        }
    }
   fun stopContainers() {
       container.stop()
   }
    fun <E> buildDao(dao: Class<E>): E {
        return jdbi.onDemand(dao)
    }
}
