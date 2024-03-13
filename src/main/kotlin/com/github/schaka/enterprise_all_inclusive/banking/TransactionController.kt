package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/{customer}/{account}/transactions")
class TransactionController(

    @Autowired val transactionService: TransactionService

) {

    @GetMapping
    fun listTransactions(
        @PathVariable("customer") customerId: Long,
        @PathVariable("account") accountId: Long,
        @PageableDefault(page = 0, size = 50, sort = ["modified", "created"]) pageable: Pageable
    ): Page<RestTransaction> {
        return transactionService.transactionHistory(customerId, accountId, pageable)
            .map(this::convert)
    }

    @PostMapping("/book")
    fun bookTransaction(
        @PathVariable("customer") customerId: Long,
        @PathVariable("account") accountId: Long,
        @Valid @RequestBody transaction: RestTransaction
    ): RestTransaction {
        return convert(
            transactionService.handleTransaction(TransactionRequest(customerId, accountId, transaction.amount, transaction.type))
        )
    }

    @PostMapping("/{id}/rollback")
    fun rollbackTransaction(
        @PathVariable("customer") customerId: Long,
        @PathVariable("account") accountId: Long,
        @PathVariable("id") transactionId: Long
    ): RestTransaction {
        return convert(
            transactionService.rollbackTransaction(customerId, accountId, transactionId)
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TransactionException::class)
    fun handleServiceError(exception: TransactionException): TransactionException {
        return exception
    }

    private fun convert(transaction: Transaction): RestTransaction {
        return RestTransaction(transaction.amount, transaction.type, transaction.status, transaction.id)
    }
}