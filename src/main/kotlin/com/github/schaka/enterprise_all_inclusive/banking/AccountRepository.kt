package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface AccountRepository : JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdAndCustomer(id: Long, customer: Customer): Account?

}