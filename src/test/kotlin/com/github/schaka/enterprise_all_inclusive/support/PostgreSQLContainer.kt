package com.github.schaka.enterprise_all_inclusive.support

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.output.OutputFrame
import java.util.Map

class PostgreSQLContainer : org.testcontainers.containers.PostgreSQLContainer<PostgreSQLContainer>("postgres:16-alpine") {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)

        private fun log(frame: OutputFrame?) {
            if (frame != null) {
              log.info("{}", frame.utf8String.trim { it <= ' ' })
            }
        }
    }

    init {
            this
            .withUsername("eai")
            .withDatabaseName("eai-test")
            .withPassword("eai")
            .withLabels(
                Map.of(
                    "project", "enterprise-all-inclusive",
                    "container-type", "eai-database-test",
                )
            )
            .withReuse(true)
            .withLogConsumer { frame -> log(frame) }
    }

    fun registerProperties(registry: DynamicPropertyRegistry) {
        // Properties for database tests.
        registry.add("spring.datasource.url") { getJdbcUrl() }
        registry.add("spring.datasource.username") { username }
        registry.add("spring.datasource.password") { password }
        registry.add("spring.datasource.hikari.maximum-pool-size") { 10 }
        registry.add("spring.datasource.hikari.minimum-idle") { 0 }
        registry.add("spring.datasource.hikari.allow-pool-suspension") { true }
        registry.add("spring.datasource.hikari.leak-detection-threshold") { 10000 }
    }
}