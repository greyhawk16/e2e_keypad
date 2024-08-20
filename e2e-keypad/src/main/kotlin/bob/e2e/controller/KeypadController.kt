package bob.e2e.controller

import bob.e2e.domain.service.KeypadService
import bob.e2e.repository.KeypadRepository
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.util.Base64
import java.security.MessageDigest


@RestController
@RequestMapping("/api")
class KeypadController {
    private var PublicKey: MutableMap<String, Any> = mutableMapOf()

    @GetMapping("/get_kaypad_secret_key")
    fun RetrieveKeypad(): ResponseEntity<Map<String, Any>> {
        val keypadRepository = KeypadRepository()
        val dbKey = "NumToHash"

        val ans = keypadRepository.retrieveHashImageMap(dbKey)

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ans)
    }

    @GetMapping("/get_public_key")
    fun getPublicKey(): ResponseEntity<MutableMap<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(PublicKey)
    }

    @GetMapping("/show_keypad")
    fun renderKeypad(): ResponseEntity<ByteArray> {
        val keypadService = KeypadService()
        val keypadNumHashes = keypadService.generateRandomHashes()  // 0~9까지의 해시값 생성
        val keypadImages = keypadService.createHashImageMap()   // 랜덤 이미지 생성 시, 숫자 별 해시값 전달하기 -> 랜덤으로 넣은 이미지별의 숫자별 해시값도 순서대로 반환하기
        // 예시: [[1, 공백,  3], [2, 4, 공백]] -> [(1의 해시값), 공백문자열, (3의 해시값), (2의 해시값), (4의 해시값), 공백문자열]반환

        // 해시값-이미지 연결한 Map 생성
        // 추후 shuffle 적용 필요
        val hashImageMap = mutableMapOf<String, String>()
        for ((key, hashValue) in keypadNumHashes) {
            hashImageMap[hashValue] = keypadImages[key] ?: ""
        }


        val dotenv = Dotenv.load()
        val dbKey = dotenv["NumToHashMap"]
        val keypadRepository = KeypadRepository()
        keypadRepository.storeHashImageMap(keypadNumHashes, dbKey)

        val temp = keypadImages.values.toList().shuffled()
        val reverseKeypadImages = keypadImages.entries.associate { (key, value) -> value to key }
        val keysForTempValues = temp.map { reverseKeypadImages[it] }

        val keypadMap = mutableMapOf<Pair<Int, Int>, String>()
        for (i in 0 until 3) {
            for (j in 0 until 4) {
                val key = keysForTempValues.getOrNull(i * 4 + j)
                val value = keypadNumHashes[key] ?: ""
                keypadMap[Pair(i, j)] = value
            }
        }

        val combinedImage = combineImages(temp)
        val md = MessageDigest.getInstance("SHA-256")
        val imageSha256 = md.digest(combinedImage).fold("") { str, it -> str + "%02x".format(it) }

        val keypadSessionId = keypadService.generateRandomHash()
        val validUntil = System.currentTimeMillis() + 45 * 1000
        val hashSalt = dotenv["HASH_SALT"]

        val hmacData = StringBuilder().apply {
            append(keypadSessionId)
            append(validUntil.toString())
            append(hashSalt)
        }.toString()
        val keypadHmac = keypadService.generateHMAC(keypadSessionId, hmacData)

        PublicKey["keypadMap"] = keypadMap
        PublicKey["keypadSessionId"] = keypadSessionId
        PublicKey["validUntil"] = validUntil
        PublicKey["keypadHmac"] = keypadHmac

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.IMAGE_PNG)
            .body(combinedImage)
    }

    @PostMapping("/verify_keypad")
    fun VerifyKeypad(@RequestBody body: Map<String, Any>): ResponseEntity<out Map<out Any?, Any?>>? {
        val requestKeypadSessionId = body["keypadSessionId"] as String
        val concatenatedHashes = body["concatenatedHashes"] as? String
        val requestKeypadHmac = body["keypadHmac"] as? String
        val keypadTimeStamp = body["validUntil"] as? Long
        val keypadRepository = KeypadRepository()
        val dbKey = "NumToHash"
        val ans = keypadRepository.retrieveHashImageMap(dbKey)
        val validUntil = PublicKey["validUntil"] as? Long ?: 0

        val dotenv = Dotenv.load()
        val hashSalt = dotenv["HASH_SALT"]
        val calculatedHmacData = StringBuilder().apply {
            append(requestKeypadSessionId)
            append(validUntil.toString())
            append(hashSalt)
        }.toString()

        val calculatedHmacKey = KeypadService().generateHMAC(requestKeypadSessionId, calculatedHmacData)

        if (requestKeypadSessionId == PublicKey["keypadSessionId"]
            && keypadTimeStamp != null
            && validUntil > System.currentTimeMillis()
            && calculatedHmacKey == requestKeypadHmac
        ) {
            val requestBody = mapOf(
                "userInput" to concatenatedHashes,
                "keyHashMap" to ans,
//                "keyLength" to 2048
            )

            val response = try {
                val restTemplate = RestTemplate()
                restTemplate.postForEntity("http://146.56.119.112:8081/auth", requestBody, Map::class.java)
                } catch (e: Exception) { ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Verification failed"))
            }
            return response
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Verification failed"))
        }

//        return if (requestKeypadSessionId == PublicKey["keypadSessionId"]
//            && keypadTimeStamp != null
//            && validUntil > System.currentTimeMillis()
//            && calculatedHmacKey == requestKeypadHmac
//            ) {
//            val requestBody = mapOf(
//                "userInput" to concatenatedHashes,
//                "keyHashMap" to ans,
//                "keyLength" to 2048
//            )
//
//            val response = try {
//                val restTemplate = RestTemplate()
//                restTemplate.postForEntity("http://146.56.119.112:8081/auth", requestBody, Map::class.java)
//            } catch (e: Exception) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("message" to "External service call failed"))
//            }
//
//            ResponseEntity.status(response.statusCode).body(response.body as Map<String, Any>)
//        } else {
//            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Verification failed"))
//        }
    }

    private fun combineImages(base64Images: List<String>): ByteArray {
        val images = base64Images.map { decodeBase64ToImage(it) }
        val rows = 3
        val cols = 4
        val width = images[0].width
        val height = images[0].height

        val combinedImage = BufferedImage(width * cols, height * rows, BufferedImage.TYPE_INT_ARGB)
        val g = combinedImage.graphics

        for (i in images.indices) {
            val x = (i % cols) * width
            val y = (i / cols) * height
            g.drawImage(images[i], x, y, null)
        }

        g.dispose()

        val baos = ByteArrayOutputStream()
        ImageIO.write(combinedImage, "png", baos)
        return baos.toByteArray()
    }

    private fun decodeBase64ToImage(base64: String): BufferedImage {
        val imageBytes = Base64.getDecoder().decode(base64)
        return ImageIO.read(ByteArrayInputStream(imageBytes))
    }
}