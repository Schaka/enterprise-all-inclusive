package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class RestTransaction(

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 4)
    val amount: BigDecimal,

    @NotNull
    val type: TransactionType,

    val status: TransactionStatus? = null,

    val id: Long? = null
)
