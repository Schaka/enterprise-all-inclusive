package com.github.schaka.enterprise_all_inclusive.tenant

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(TenantProperties::class)
@Configuration
class TenantConfig(
    val tenantProperties: TenantProperties
) {

    @Bean
    fun flywayCustomizer(): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer { config -> config?.schemas(getSchema()) }
    }

    @Bean
    fun tenantTable(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { config -> config?.put("hibernate.default_schema", getSchema()) }
    }

    private fun getSchema(): String {
        val validSchema = tenantProperties.name?.replace(Regex("[ |\\-|\\.]"), "_")
        return "tenant_${validSchema}"
    }
}