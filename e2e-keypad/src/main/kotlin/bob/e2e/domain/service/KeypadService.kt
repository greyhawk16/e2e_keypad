package bob.e2e.domain.service


import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.Base64
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class KeypadService {
    fun getImages(): Map<String, String> {
        return try {
            val resource = ClassPathResource("static/keypad")
            val imageFiles = resource.file.listFiles { file -> file.extension == "png" }

            if (imageFiles != null) {
                imageFiles.associate { file ->
                    val imageBytes = file.readBytes()
                    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
                    file.nameWithoutExtension to base64Image
                }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun generateRandomHashes(): Map<String, String> {
        val usedHashes = mutableSetOf<String>()
        val map = (0..9).associate { key ->
            val keyString = key.toString()
            var hash: String
            do {
                hash = generateRandomHash()
            } while (hash in usedHashes)
            usedHashes.add(hash)
            keyString to hash
        }
        return map
    }

    fun generateRandomHash(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun generateHMAC(key: String, data: String): String {
        // Convert the key to a byte array
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")

        // Create a Mac instance and initialize it with the secret key
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)

        // Compute the HMAC
        val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))

        // Encode the HMAC bytes to a Base64 string
        return Base64.getEncoder().encodeToString(hmacBytes)
    }

    // 세션이랑 키패드의 식별자 만드는 함수
    fun createSessionAndKeypadId(): Pair<String, String> {
        val session = generateRandomHash()
        val keypadId = generateRandomHash()
        return session to keypadId
    }



}