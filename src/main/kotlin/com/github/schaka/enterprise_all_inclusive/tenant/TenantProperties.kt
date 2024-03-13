package com.github.schaka.enterprise_all_inclusive.tenant

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tenant")
data class TenantProperties(
    var name: String?,
)