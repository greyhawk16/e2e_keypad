package bob.e2e

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

// 참고 사이트: https://itmoon.tistory.com/124?category=948164
@SpringBootApplication
class E2eKeypadApplication {
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000")
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<E2eKeypadApplication>(*args)
}