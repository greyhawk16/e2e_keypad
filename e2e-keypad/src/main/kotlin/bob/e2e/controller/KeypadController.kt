package bob.e2e.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    fun getImageAndHash(): ResponseEntity<Any> {
        val resource = ClassPathResource("keypad.jpg")

        // Calculate the hash
        val hashValue = calculateHash(resource)

        // Set headers for the image response
        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG

        // Create the response
        val imageResource = InputStreamResource(resource.inputStream)
        return ResponseEntity
            .status(HttpStatus.OK)
            .header("X-Image-Hash", hashValue)
            .headers(headers)
            .body(imageResource)
    }

    private fun calculateHash(resource: ClassPathResource): String {
        return "0"
    }
}