package com.example.condsuites.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "finance_transactions")
data class FinanceTransactionEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "PAYABLE",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Double = 0.0,
    @get:PropertyName("dueDate") @set:PropertyName("dueDate") var dueDate: String = "",
    @get:PropertyName("isPaid") @set:PropertyName("isPaid") var isPaid: Boolean = false,
    @get:PropertyName("paidDate") @set:PropertyName("paidDate") var paidDate: String? = null,
    @get:PropertyName("installmentNumber") @set:PropertyName("installmentNumber") var installmentNumber: Int = 1,
    @get:PropertyName("totalInstallments") @set:PropertyName("totalInstallments") var totalInstallments: Int = 1,
    @get:PropertyName("groupId") @set:PropertyName("groupId") var groupId: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("relatedId") @set:PropertyName("relatedId") var relatedId: Long? = null
)
