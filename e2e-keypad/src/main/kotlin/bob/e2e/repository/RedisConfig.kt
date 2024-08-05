package bob.e2e.repository

import redis.clients.jedis.Jedis


class RedisConfig {
    private val jedis: Jedis = Jedis("localhost", 6379)

    fun getJedis(): Jedis {
        return jedis
    }
}