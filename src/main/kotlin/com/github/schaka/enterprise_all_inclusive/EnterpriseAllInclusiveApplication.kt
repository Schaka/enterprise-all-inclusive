package com.github.schaka.enterprise_all_inclusive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties
@EnableAsync
@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
class EnterpriseAllInclusiveApplication

fun main(args: Array<String>) {
    runApplication<EnterpriseAllInclusiveApplication>(*args)
}
