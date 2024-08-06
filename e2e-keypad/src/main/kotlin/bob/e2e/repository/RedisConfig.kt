package bob.e2e.repository

import io.github.cdimascio.dotenv.Dotenv
import redis.clients.jedis.Jedis


class RedisConfig {
    private val dotenv = Dotenv.load()
    private val dbHost = dotenv["DB_HOST"]
    private val dbPort = dotenv["DB_PORT"]
    private val jedis: Jedis = Jedis(dbHost, dbPort.toInt())

    fun getJedis(): Jedis {
        return jedis
    }
}