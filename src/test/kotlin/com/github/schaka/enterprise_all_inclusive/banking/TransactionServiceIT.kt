package com.github.schaka.enterprise_all_inclusive.banking

import com.github.schaka.enterprise_all_inclusive.banking.TransactionStatus.*
import com.github.schaka.enterprise_all_inclusive.banking.TransactionType.IN
import com.github.schaka.enterprise_all_inclusive.banking.TransactionType.OUT
import com.github.schaka.enterprise_all_inclusive.support.PostgreSQLContainer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch

@SpringBootTest(webEnvironment = NONE)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionServiceIT(

    @Autowired val transactionService: TransactionService,
    @Autowired val customerRepository: CustomerRepository,
    @Autowired val accountRepository: AccountRepository,
    @Autowired val transactionRepository: TransactionRepository

    ) {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer? = PostgreSQLContainer()

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            postgres?.registerProperties(registry)
        }
    }

    @BeforeEach
    fun setup() {
        transactionRepository.deleteAll()
        accountRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun throwsExceptionOnInvalidCustomer() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(Long.MAX_VALUE, account.id!!, BigDecimal("10"), IN)

        assertThatThrownBy {
            transactionService.handleTransaction(request)
        }
            .isInstanceOf(TransactionException::class.java)
            .hasMessageContaining("Invalid customer")
    }

    @Test
    fun throwsExceptionOnInvalidAccount() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(account.customer.id!!, Long.MAX_VALUE, BigDecimal("10"), IN)

        assertThatThrownBy {
            transactionService.handleTransaction(request)
        }
            .isInstanceOf(TransactionException::class.java)
            .hasMessageContaining("Invalid account")
    }

    @Test
    fun rejectsTransactionsOverBudget() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(account.customer.id!!, account.id!!, BigDecimal("120"), OUT)
        val transaction = transactionService.handleTransaction(request)
        val accountAfterModification = accountRepository.findById(account.id!!).get()

        assertThat(transaction.status).isEqualTo(REJECTED)
        assertThat(accountAfterModification.balance.toInt()).isEqualTo(100)
    }

    @Test
    fun allowsTransactionsWithinBudget() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(account.customer.id!!, account.id!!, BigDecimal("100"), OUT)
        val transaction = transactionService.handleTransaction(request)
        val accountAfterModification = accountRepository.findById(account.id!!).get()

        assertThat(transaction.status).isEqualTo(ACCEPTED)
        assertThat(accountAfterModification.balance.toInt()).isEqualTo(0)
    }

    @Test
    fun rollsBackTransactionSuccessfully() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(account.customer.id!!, account.id!!, BigDecimal("100"), OUT)
        val transaction = transactionService.handleTransaction(request)
        assertThat(transaction.status).isEqualTo(ACCEPTED)
        
        val rollbackTransaction = transactionService.rollbackTransaction(account.customer.id!!, account.id!!, transaction.id!!)
        assertThat(rollbackTransaction.status).isEqualTo(VOID)
        
        val accountAfterModification = accountRepository.findById(account.id!!).get()

        assertThat(transaction.status).isEqualTo(ACCEPTED)
        assertThat(accountAfterModification.balance.toInt()).isEqualTo(100)
    }

    @Test
    fun rollbackFailsOnRejectedTransaction() {
        val account = givenCustomerWithBudget("100")
        val request = TransactionRequest(account.customer.id!!, account.id!!, BigDecimal("120"), OUT)
        val transaction = transactionService.handleTransaction(request)
        assertThat(transaction.status).isEqualTo(REJECTED)

        assertThatThrownBy { transactionService.rollbackTransaction(account.customer.id!!, account.id!!, transaction.id!!)  }
            .isInstanceOf(TransactionException::class.java)
            .hasMessageContaining("Invalid transaction")

        val transactionAfterModification = transactionRepository.findById(transaction.id!!).get()
        assertThat(transactionAfterModification.status).isEqualTo(REJECTED)

        val accountAfterModification = accountRepository.findById(account.id!!).get()
        assertThat(accountAfterModification.balance.toInt()).isEqualTo(100)
    }

    @Test
    fun testMultithreadHandling() {
        val account = givenCustomerWithBudget("100")

        val workers = whenTransactionsAreProcessed(account, "15", "20")

        // Hibernate doesn't update what was modified outside this thread, as the account is already detached that point
        val accountAfterModification = accountRepository.findById(account.id!!).get()
        assertThat(accountAfterModification.balance).isEqualTo("135.0000")

        val transactionOrderMatches = thenTransactionOrderMatches(account, workers)
        assertThat(transactionOrderMatches).isTrue()

    }

    fun givenCustomerWithBudget(budget: String): Account {
        val customer = customerRepository.save(Customer("customer1"))
        return accountRepository.save(Account("savings", customer, BigDecimal(budget)))
    }

    fun whenTransactionsAreProcessed(account: Account, vararg transactions: String): List<TransactionWorker> {
        // if locking doesn't work correctly, we may end up with 120 or 115 instead of the correct 135
        // since the database is the part handling concurrency, we don't have to further worry about multiple instances of the same application running in parallel
        val latch = CountDownLatch(1)
        val transactionWorkers = transactions.map {
            val request = TransactionRequest(account.customer.id!!, account.id!!, BigDecimal(it), IN)
            val transactionThread = TransactionWorker("transaction-${it}", { transactionService.handleTransaction(request) }, request, latch)
            transactionThread.start()
            transactionThread
        }.toList()

        latch.countDown()

        // wait for both threads to finish
        transactionWorkers.forEach { it.join() }
        return transactionWorkers
    }

    /**
     * There's no guarantee which thread starts first - the perfect scenario for this test.
     *  We'll validate which thread finishes work first and then validate against the transactions in the database.
     *
     *  Since the workers lack a clear identifier, we'll use the transaction amount itself. This is good enough for a test.
     */
    fun thenTransactionOrderMatches(account: Account, workers: List<TransactionWorker>): Boolean {
        val transactions = transactionRepository.findByAccount(account).sortedBy { it.created }.map { it.amount.toInt() }
        val transactionWorkers = workers.sortedBy { it.finished }.map { it.request.amount.toInt() }
        return transactions == transactionWorkers
    }

    class TransactionWorker(
        name: String,
        val work: () -> Unit,
        val request: TransactionRequest,
        val latch: CountDownLatch,
        var finished: LocalDateTime? = null
    ) : Thread(name) {
        override fun run() {
            try {
                latch.await()
                work()
                finished = LocalDateTime.now()
            } catch (e: InterruptedException) {
                // ignore
            }
        }
    }
}