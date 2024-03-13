package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "accounts")
class Account(

    val name: String,

    @JoinColumn(name = "customer_id")
    @ManyToOne(fetch = FetchType.EAGER)
    val customer: Customer,

    var balance: BigDecimal,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", targetEntity = Transaction::class)
    val transactions: List<Transaction> = listOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}