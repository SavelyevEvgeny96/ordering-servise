package ru.sogaz.site.ordering_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderingServiceApplication

fun main(args: Array<String>) {
	runApplication<OrderingServiceApplication>(*args)
}
