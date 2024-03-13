package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.persistence.*

@Entity
@Table(name = "customers")
class Customer(

    val name: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    val accounts: List<Account> = listOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}