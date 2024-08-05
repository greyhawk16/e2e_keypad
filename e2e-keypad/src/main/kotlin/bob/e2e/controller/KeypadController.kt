package bob.e2e.controller

import bob.e2e.domain.service.KeypadService
import bob.e2e.repository.KeypadRepository

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


@RestController
@RequestMapping("/keypad")
class KeypadController {
    // GET 요청 -> 키패드 식별자, 숫자쌍 & 키패드 이미지, HMAC 전송
    @GetMapping("/create_keypad")
    fun getImageAndHash(): ResponseEntity<Map<String, Any>> {
        val keypadService = KeypadService()
        val keypadImages = keypadService.getImages()
        val keypadHashes = keypadService.generateRandomHashes()

        // Create the JSON response
        // Create the new dictionary
        val hashImageMap = mutableMapOf<String, String>()
        for ((key, hashValue) in keypadHashes) {
            hashImageMap[hashValue] = keypadImages[key] ?: ""
        }

        val keypadSessionId = keypadService.generateRandomHash()
        val hmacKey = "abcde12345" // test
        val keypadHmac = keypadService.generateHMAC(keypadSessionId, hmacKey)

        val responseBody = mapOf(
            "keypad" to hashImageMap,
            "HMAC" to keypadHmac,
            "keypadSessionId" to keypadSessionId
        )

        val keypadRepository = KeypadRepository()
        keypadRepository.storeHashImageMap(hashImageMap)

        // Create the response body
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody)
    }

    @GetMapping("/retrieve_keypad")
    fun RetrieveHashImageMap(): ResponseEntity<Map<String, Any>> {
        val keypadRepository = KeypadRepository()
        val ans = keypadRepository.retrieveHashImageMap()

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ans)
    }
}