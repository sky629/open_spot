package com.kangpark.openspot.analysis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication(scanBasePackages = ["com.kangpark.openspot"])
@EnableJpaAuditing
@EntityScan(basePackages = ["com.kangpark.openspot"])
@EnableJpaRepositories(basePackages = ["com.kangpark.openspot"])
@EnableKafka
class AnalysisServiceApplication

fun main(args: Array<String>) {
    runApplication<AnalysisServiceApplication>(*args)
}