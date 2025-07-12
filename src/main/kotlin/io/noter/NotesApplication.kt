package io.noter

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(info = Info(title = "Noter App API", version = "v1", description = "APIs for Notes Application"),
	security = [SecurityRequirement(name = "Bearer Authentication"),
	]
)
@SecurityScheme(type = SecuritySchemeType.HTTP, name = "Bearer Authentication", `in` = SecuritySchemeIn.HEADER, scheme = "bearer", bearerFormat = "JWT",
	description = "Please enter JWT token in the format")
class NotesApplication

fun main(args: Array<String>) {
	runApplication<NotesApplication>(*args)
}
