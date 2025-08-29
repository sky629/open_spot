package com.kangpark.openspot.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.kangpark.openspot"])
@EnableJpaAuditing
@EntityScan(basePackages = ["com.kangpark.openspot"])
@EnableJpaRepositories(basePackages = ["com.kangpark.openspot"])
class AuthServiceApplication

fun main(args: Array<String>) {
    runApplication<AuthServiceApplication>(*args)
}