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
            CREATE TABLE GAME (
                gameId SERIAL PRIMARY KEY,
                gameName VARCHAR(50) not null,
                windowStart TIMESTAMPTZ not null,
                windowClose TIMESTAMPTZ not null,
                guessesClose TIMESTAMPTZ not null,
                deliveryTime TIMESTAMPTZ null,
                userId VARCHAR(50) not null,
                gameActive BOOLEAN not null
            )
        """.trimIndent())
        batch.add("""
            CREATE OR REPLACE FUNCTION check_game_active_and_userid()
            RETURNS TRIGGER AS ${'$'}${'$'}
            BEGIN
                IF NOT (NEW.deliverytime BETWEEN (SELECT windowstart FROM game WHERE gameid = NEW.gameid) AND (SELECT windowclose FROM game WHERE gameid = NEW.gameid)) THEN
                    RAISE EXCEPTION 'Delivery time % is not between start and closing window range of the game', NEW.deliverytime USING ERRCODE = 'ERRG0';
                END IF;
                RETURN NEW;
            END;
            ${'$'}${'$'} LANGUAGE plpgsql;
        """.trimIndent())
        batch.add("""
            CREATE TRIGGER check_game_active_and_userid_trigger
            BEFORE UPDATE ON GAME
            FOR EACH ROW
            EXECUTE FUNCTION check_game_active_and_userid();
        """.trimIndent())
        batch.add("""
            CREATE TABLE GUESS (
                guessId SERIAL PRIMARY KEY,
                gameId INT not null,
                userId VARCHAR(50) not null,
                guessTime TIMESTAMPTZ not null,
                CONSTRAINT fk_gameid
                    FOREIGN KEY (gameId) 
                        REFERENCES GAME(gameId)
                        ON DELETE CASCADE,
                CONSTRAINT game_and_guess_time UNIQUE (gameId, guessTime)
            )
        """.trimIndent())
        batch.add("""
            CREATE OR REPLACE FUNCTION check_gameid_and_guesstime()
            RETURNS TRIGGER AS ${'$'}${'$'}
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM game WHERE gameid = NEW.gameid) THEN
                    RAISE EXCEPTION 'Game ID % does not exist.', NEW.gameid USING ERRCODE = 'ERRA0';
                END IF;
                IF NOT (NEW.guesstime BETWEEN (SELECT windowstart FROM game WHERE gameid = NEW.gameid) AND (SELECT windowclose FROM game WHERE gameid = NEW.gameid)) THEN
                    RAISE EXCEPTION'Guess time % is not between start and closing window range of the game', NEW.guesstime USING ERRCODE = 'ERRA1';
                END IF;
                RETURN NEW;
            END;
            ${'$'}${'$'} LANGUAGE plpgsql;
        """.trimIndent())
        batch.add("""
            CREATE TRIGGER check_gameid_and_guesstime_trigger
            BEFORE INSERT ON GUESS
            FOR EACH ROW
            EXECUTE FUNCTION check_gameid_and_guesstime();
        """.trimIndent())
        batch.add("""
            CREATE TABLE WIN (
                winId SERIAL PRIMARY KEY,
                gameId INT not null,
                guessId INT not null,
                date TIMESTAMPTZ not null,
                CONSTRAINT fk_gameid
                    FOREIGN KEY (gameId) 
                        REFERENCES GAME(gameId)
                        ON DELETE CASCADE,
                CONSTRAINT fk_guessid
                    FOREIGN KEY (guessId)
                        REFERENCES GUESS(guessId)
                        ON DELETE CASCADE,
                CONSTRAINT game_and_guess_unique UNIQUE (gameId, guessId)
            )
        """.trimIndent())
        batch.add("""
            CREATE TABLE POINT (
                pointId SERIAL PRIMARY KEY,
                userId VARCHAR(50) not null,
                played INT not null,
                won INT not null,
                lost INT not null,
                totalPoint INT not null,
                CONSTRAINT unique_user_id UNIQUE (userId)
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
