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
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.util.Base64
import java.io.File


@RestController
@RequestMapping("/api")
class KeypadController {
    private var PublicKey: ArrayList<ArrayList<String?>> = arrayListOf()

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
    fun getPublicKey(): ResponseEntity<ArrayList<ArrayList<String?>>> {
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

        val hmacKey = keypadService.generateRandomHash()
        val keypadSessionId = keypadService.generateRandomHash()
        val keypadHmac = keypadService.generateHMAC(keypadSessionId, hmacKey)

        val dotenv = Dotenv.load()
        val dbKey = dotenv["NumToHashMap"]
        val keypadRepository = KeypadRepository()
        keypadRepository.storeHashImageMap(keypadNumHashes, dbKey)

        val temp = keypadImages.values.toList().shuffled()
        val reverseKeypadImages = keypadImages.entries.associate { (key, value) -> value to key }
        val keysForTempValues = temp.map { reverseKeypadImages[it] }

        // Create 4-by-3 ArrayList with corresponding values from keypadNumHashes
        PublicKey = ArrayList()
        for (i in 0 until 3) {
            val row = ArrayList<String?>()
            for (j in 0 until 4) {
                val key = keysForTempValues.getOrNull(i * 4 + j)
                val value = keypadNumHashes[key] ?: ""
                row.add(value)
            }
            PublicKey.add(row)
        }

        val temp2 = PublicKey
        val combinedImage = combineImages(temp)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.IMAGE_PNG)
            .body(combinedImage)
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