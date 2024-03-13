package com.github.schaka.enterprise_all_inclusive.banking

import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, Long> {

}