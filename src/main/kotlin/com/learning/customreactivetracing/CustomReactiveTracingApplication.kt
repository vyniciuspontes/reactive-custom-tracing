package com.learning.customreactivetracing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomTracingApplication

fun main(args: Array<String>) {
	runApplication<CustomTracingApplication>(*args)
}
