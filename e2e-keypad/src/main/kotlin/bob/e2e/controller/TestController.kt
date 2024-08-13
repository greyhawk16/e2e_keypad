package bob.e2e.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api_test/example")
class TestController {
    @GetMapping
    fun getExample(): List<String> {
        return listOf("Hello", "World")
    }
}