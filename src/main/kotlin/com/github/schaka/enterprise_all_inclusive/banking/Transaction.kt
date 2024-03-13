package com.github.schaka.enterprise_all_inclusive.banking

import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.TemporalType.TIMESTAMP
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "transactions")
class Transaction(

    @JoinColumn(name = "account_id")
    @ManyToOne(fetch = FetchType.EAGER)
    val account: Account,

    val amount: BigDecimal,

    @Enumerated(STRING)
    var status: TransactionStatus,

    @Enumerated(STRING)
    val type: TransactionType,

    @CreatedDate
    @Column(name = "created")
    @Temporal(TIMESTAMP)
    var created: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "modified")
    @Temporal(TIMESTAMP)
    var modified: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}