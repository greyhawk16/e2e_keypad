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


@RestController
@RequestMapping("/keypad")
class KeypadController {
    // 추후: POST 요청 & HMAC에 쓸 KEY 전송 -> 키패드 세션 식별자, 숫자쌍 & 키패드 이미지, HMAC 반환
    @PostMapping("/create_keypad")
    fun CreateKeypad(@RequestBody hmacKey: String): ResponseEntity<Map<String, Any>> {
        val keypadService = KeypadService()
        val keypadImages = keypadService.createHashImageMap()
        val keypadNumHashes = keypadService.generateRandomHashes()

        // 해시값-이미지 연결한 Map 생성
        // 추후 shuffle 적용 필요
        val hashImageMap = mutableMapOf<String, String>()
        for ((key, hashValue) in keypadNumHashes) {
            hashImageMap[hashValue] = keypadImages[key] ?: ""
        }

        val keypadSessionId = keypadService.generateRandomHash()
        val keypadHmac = keypadService.generateHMAC(keypadSessionId, hmacKey)

        val responseBody = mapOf(
            "keypad" to hashImageMap,
            "HMAC" to keypadHmac,
            "keypadSessionId" to keypadSessionId
        )

        val dotenv = Dotenv.load()
        val dbKey = dotenv["NumToHashMap"]
        val keypadRepository = KeypadRepository()
        keypadRepository.storeHashImageMap(keypadNumHashes, dbKey)

        // Create the response body
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody)
    }

    @GetMapping("/retrieve_keypad")
    fun RetrieveKeypad(): ResponseEntity<Map<String, Any>> {
        val keypadRepository = KeypadRepository()
        val dbKey = "NumToHash"

        val ans = keypadRepository.retrieveHashImageMap(dbKey)

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ans)
    }
}