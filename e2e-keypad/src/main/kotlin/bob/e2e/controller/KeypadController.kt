package bob.e2e.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.security.MessageDigest
import java.util.Base64


@RestController
@RequestMapping("/keypad")
class KeypadController {
    // GET 요청 -> 키패드 해시값, 키패드 이미지 전송
    @GetMapping("/retrieve_keypad")
    fun getImageAndHash(): ResponseEntity<Map<String, Any>> {
        val resource = ClassPathResource("static/keypad/_0.png")

        // Calculate the hash
        val hashValue = calculateHash(resource)

        // Read and encode the image to Base64
        val imageStream = resource.inputStream
        val imageBytes = StreamUtils.copyToByteArray(imageStream)
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)

        // Create the JSON response
        val responseBody = mapOf(
            "image" to base64Image,
            "hash" to hashValue
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody)
    }

    private fun calculateHash(resource: ClassPathResource): String {
        return "0"
    }
}