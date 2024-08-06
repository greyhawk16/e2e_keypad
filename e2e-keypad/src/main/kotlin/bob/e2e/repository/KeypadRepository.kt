package bob.e2e.repository

class KeypadRepository {
    private val redisConfig = RedisConfig()
    private val jedis = redisConfig.getJedis()

    fun storeHashImageMap(hashImageMap: Map<String, String>, dbKey: String) {

        val current = jedis.hgetAll(dbKey)
        if (current.isNotEmpty()) {
            jedis.del(dbKey)
        }

        hashImageMap.forEach { (key, value) ->
            jedis.hset(dbKey, key, value)
        }
    }

    fun retrieveHashImageMap(dbKey: String): Map<String, String> {
        return jedis.hgetAll(dbKey)
    }

    fun clearAllData() {
        jedis.flushAll()
    }
}