package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "service_orders")
data class ServiceOrderEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("floor") @set:PropertyName("floor") var floor: String = "",
    @get:PropertyName("isInstallment") @set:PropertyName("isInstallment") var isInstallment: Boolean = false,
    @get:PropertyName("totalValue") @set:PropertyName("totalValue") var totalValue: Double = 0.0,
    @get:PropertyName("type") @set:PropertyName("type") var type: String = ""
)

@Entity(
    tableName = "service_installments",
    foreignKeys = [
        ForeignKey(
            entity = ServiceOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceOrderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serviceOrderId")]
)
data class ServiceInstallmentEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("serviceOrderId") @set:PropertyName("serviceOrderId") var serviceOrderId: Long = 0,
    @get:PropertyName("number") @set:PropertyName("number") var number: Int = 0,
    @get:PropertyName("value") @set:PropertyName("value") var value: Double = 0.0,
    @get:PropertyName("dueDate") @set:PropertyName("dueDate") var dueDate: String = "",
    @get:PropertyName("isPaid") @set:PropertyName("isPaid") var isPaid: Boolean = false
)

data class ServiceOrderWithInstallments(
    @Embedded val order: ServiceOrderEntity,
    @Relation(parentColumn = "id", entityColumn = "serviceOrderId")
    val installments: List<ServiceInstallmentEntity>
)
