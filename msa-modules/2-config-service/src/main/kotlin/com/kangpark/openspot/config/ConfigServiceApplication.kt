package com.kangpark.openspot.config

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration


@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
@EnableConfigServer
class ConfigServiceApplication

fun main(args: Array<String>) {
    runApplication<ConfigServiceApplication>(*args)
}