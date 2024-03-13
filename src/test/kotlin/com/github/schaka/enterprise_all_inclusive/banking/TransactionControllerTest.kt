package com.github.schaka.enterprise_all_inclusive.banking

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.schaka.enterprise_all_inclusive.banking.TransactionStatus.ACCEPTED
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest(

    @Autowired
    val mockMvc: MockMvc,

    @Autowired
    val json: ObjectMapper

) {

    @MockkBean
    lateinit var accountRepository: AccountRepository

    @MockkBean
    lateinit var customerRepository: CustomerRepository

    @MockkBean
    lateinit var transactionRepository: TransactionRepository

    val customer = Customer("customer", listOf(), 1)

    val account = Account("account", customer, BigDecimal("100"), listOf(), 1)

    @BeforeEach
    fun setup() {
        every { customerRepository.findById(1) } returns Optional.of(customer)
        every { customerRepository.findById(1000) } returns Optional.empty()

        every { accountRepository.findById(1) } returns Optional.of(account)
        every { accountRepository.findById(1000) } returns Optional.empty()
        every { accountRepository.findByIdAndCustomer(any(), customer) } returns account
        every { accountRepository.findByIdAndCustomer(1000, customer) } returns null

        val transaction1 = Transaction(account, BigDecimal("15"), ACCEPTED, TransactionType.IN, id = 1)
        val transaction2 = Transaction(account, BigDecimal("60"), ACCEPTED, TransactionType.IN, id = 2)
        val transaction3 = Transaction(account, BigDecimal("25"), ACCEPTED, TransactionType.IN, id = 3)
        val transactions = listOf(transaction1, transaction2, transaction3)

        every { transactionRepository.findByAccount(eq(account)) } returns transactions
        every { transactionRepository.findByAccount(account, any(Pageable::class)) } returns PageImpl(transactions, PageRequest.of(0, 50), 3)
        every { transactionRepository.findByIdAndAccountAndStatus(1, account, any()) } returns transaction1
    }

    @Test
    fun historyBadRequestOnInvalidCustomer() {
        mockMvc.perform(get("/api/1000/1/transactions"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid customer: 1000"))
    }

    @Test
    fun historyBadRequestOnInvalidAccount() {
        mockMvc.perform(get("/api/1/1000/transactions"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid account: 1000"))
    }

    @Test
    fun listsTransactionsHistorically() {
        mockMvc.perform(get("/api/1/1/transactions"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            //.andExpect(jsonPath("\$.content", hasSize(3)))
            .andExpect(jsonPath("\$.numberOfElements").value("3"))
            .andExpect(jsonPath("\$.totalElements").value("3"))
            .andExpect(jsonPath("\$.number").value("0"))
            .andExpect(jsonPath("\$.size").value("50"))
    }

    @Test
    fun bookBadRequestOnInvalidCustomer() {
        mockMvc.perform(
            post("/api/1000/1/transactions/book")
                .contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(RestTransaction(BigDecimal("100"), TransactionType.IN)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid customer: 1000"))
    }

    @Test
    fun bookBadRequestOnInvalidAccount() {
        mockMvc.perform(
            post("/api/1/1000/transactions/book")
                .contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(RestTransaction(BigDecimal("100"), TransactionType.IN)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid account: 1000"))
    }

    @Test
    fun bookSuccessfully() {
        every { transactionRepository.save(any()) } returns Transaction(account, BigDecimal("15"), ACCEPTED, TransactionType.IN, id = 5)

        mockMvc.perform(
            post("/api/1/1/transactions/book")
                .contentType(APPLICATION_JSON)
                .content(json.writeValueAsString(RestTransaction(BigDecimal("100"), TransactionType.IN)))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.id").value("5"))
            .andExpect(jsonPath("\$.status").value("ACCEPTED"))
    }

    @Test
    fun rollbackBadRequestOnInvalidCustomer() {
        mockMvc.perform(
            post("/api/1000/1/transactions/1/rollback")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid customer: 1000"))
    }

    @Test
    fun rollbackBadRequestOnInvalidAccount() {
        mockMvc.perform(
            post("/api/1/1000/transactions/1/rollback")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid account: 1000"))
    }

    @Test
    fun rollbackUnsuccessfully() {
        every { transactionRepository.findByIdAndAccountAndStatus(any(), any(), any()) } returns null

        mockMvc.perform(
            post("/api/1/1/transactions/1000/rollback")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.message").value("Invalid transaction: 1000"))
    }

    @Test
    fun rollbackSuccessfully() {
        every { transactionRepository.findByIdAndAccountAndStatus(any(), any(), any()) } returns Transaction(account, BigDecimal("15"), ACCEPTED, TransactionType.IN, id = 1)

        mockMvc.perform(
            post("/api/1/1/transactions/1/rollback")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("\$.id").value("1"))
            .andExpect(jsonPath("\$.status").value("VOID"))
    }
}

