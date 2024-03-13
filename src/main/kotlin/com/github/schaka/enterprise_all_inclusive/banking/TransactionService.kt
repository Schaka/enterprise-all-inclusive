package com.github.schaka.enterprise_all_inclusive.banking

import com.github.schaka.enterprise_all_inclusive.banking.TransactionStatus.*
import com.github.schaka.enterprise_all_inclusive.banking.TransactionType.IN
import com.github.schaka.enterprise_all_inclusive.banking.TransactionType.OUT
import jakarta.transaction.Transactional
import jakarta.transaction.Transactional.TxType.REQUIRES_NEW
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransactionService(
    private val customerRepository: CustomerRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    /**
     * This locks the targeted account pessimistically, so that we may modify the balance without any other thread or transaction getting in our way.
     * The lock is released when transaction is committed after the method ends.
     */
    @Transactional(REQUIRES_NEW)
    fun handleTransaction(request: TransactionRequest): Transaction {
        val account = getAccount(request.customerId, request.accountId)

        val newBalance = when (request.type) {
            IN -> account.balance.add(request.amount)
            OUT -> account.balance.subtract(request.amount)
        }

        var status = REJECTED
        if (newBalance >= BigDecimal.ZERO) {
            status = ACCEPTED
            account.balance = newBalance
        }

        val transaction = Transaction(account, request.amount, status, request.type)
        return transactionRepository.save(transaction)
    }

    @Transactional(REQUIRES_NEW)
    fun rollbackTransaction(customerId: Long, accountId: Long, transactionId: Long): Transaction {
        val account = getAccount(customerId, accountId)
        // don't even bother retrieving the transaction if it can't be reverted (REJECTED, VOID)
        val transaction = transactionRepository.findByIdAndAccountAndStatus(transactionId, account, ACCEPTED) ?: throw TransactionException("Invalid transaction: $transactionId")

        val newBalance = when (transaction.type) {
            OUT -> account.balance.add(transaction.amount)
            IN -> account.balance.subtract(transaction.amount)
        }

        transaction.status = VOID
        account.balance = newBalance
        return transaction
    }

    fun transactionHistory(customerId: Long, accountId: Long, pageable: Pageable): Page<Transaction> {
        val account = getAccount(customerId, accountId)
        return transactionRepository.findByAccount(account, pageable)
    }

    private fun getAccount(customerId: Long, accountId: Long): Account {
        val customer = customerRepository.findById(customerId).orElseThrow { TransactionException("Invalid customer: $customerId") }
        return accountRepository.findByIdAndCustomer(accountId, customer) ?: throw TransactionException("Invalid account: $accountId")
    }
}