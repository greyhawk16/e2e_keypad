package bob.e2e.repository

class KeypadRepository {
    private val redisConfig = RedisConfig()
    private val jedis = redisConfig.getJedis()

    fun storeHashImageMap(hashImageMap: Map<String, String>) {
        hashImageMap.forEach { (key, value) ->
            jedis.hset("HashImageMap", key, value)
        }
    }

    fun retrieveHashImageMap(): Map<String, String> {
        return jedis.hgetAll("HashImageMap")
    }
}