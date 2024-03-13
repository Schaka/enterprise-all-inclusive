package com.github.schaka.enterprise_all_inclusive.banking

import java.math.BigDecimal

data class TransactionRequest(
    val customerId: Long,
    val accountId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val id: Long? = null
)
