package com.github.schaka.enterprise_all_inclusive.banking

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionRepository : JpaRepository<Transaction, Long> {

    fun findByAccount(account: Account): List<Transaction>

    fun findByIdAndAccountAndStatus(id: Long, account: Account, status: TransactionStatus): Transaction?

    fun findByAccount(account: Account, pageable: Pageable): Page<Transaction>

}