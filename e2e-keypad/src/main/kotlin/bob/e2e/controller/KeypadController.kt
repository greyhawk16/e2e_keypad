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


@RestController
@RequestMapping("/keypad")
class KeypadController {
    @GetMapping("/keypad_information")
    fun RetrieveKeypad(): ResponseEntity<Map<String, Any>> {
        val keypadRepository = KeypadRepository()
        val dbKey = "NumToHash"

        val ans = keypadRepository.retrieveHashImageMap(dbKey)

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ans)
    }

    @GetMapping("/render_keypad")
    fun renderKeypad(): ResponseEntity<ByteArray> {
        val keypadService = KeypadService()
        val keypadImages = keypadService.createHashImageMap()
        val keypadNumHashes = keypadService.generateRandomHashes()

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

        val combinedImage = combineImages(keypadImages.values.toList().shuffled())

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.IMAGE_PNG)
            .body(combinedImage)
    }

    private fun combineImages(base64Images: List<String>): ByteArray {
        val images = base64Images.map { decodeBase64ToImage(it) }
        val cols = 3
        val rows = 4
        val width = images[0].width
        val height = images[0].height

        val combinedImage = BufferedImage(width * rows, height * cols, BufferedImage.TYPE_INT_ARGB)
        val g = combinedImage.graphics

        for (i in images.indices) {
            val x = (i % rows) * width
            val y = (i / rows) * height
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