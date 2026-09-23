package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "agreements")
data class AgreementEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("totalDebt") @set:PropertyName("totalDebt") var totalDebt: Double = 0.0,
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("installmentsCount") @set:PropertyName("installmentsCount") var installmentsCount: Int = 1,
    @get:PropertyName("installmentValue") @set:PropertyName("installmentValue") var installmentValue: Double = 0.0,
    @get:PropertyName("isArchived") @set:PropertyName("isArchived") var isArchived: Boolean = false,
    @get:PropertyName("originalAgreementId") @set:PropertyName("originalAgreementId") var originalAgreementId: Long? = null,
    @get:PropertyName("isLawsuit") @set:PropertyName("isLawsuit") var isLawsuit: Boolean = false,
    @get:PropertyName("n1Date") @set:PropertyName("n1Date") var n1Date: String? = null,
    @get:PropertyName("n2Date") @set:PropertyName("n2Date") var n2Date: String? = null,
    @get:PropertyName("n3Date") @set:PropertyName("n3Date") var n3Date: String? = null,
    @get:PropertyName("quotaMonths") @set:PropertyName("quotaMonths") var quotaMonths: String = ""
)

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = AgreementEntity::class,
            parentColumns = ["id"],
            childColumns = ["agreementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("agreementId")]
)
data class InstallmentEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("agreementId") @set:PropertyName("agreementId") var agreementId: Long = 0,
    @get:PropertyName("number") @set:PropertyName("number") var number: Int = 0,
    @get:PropertyName("value") @set:PropertyName("value") var value: Double = 0.0,
    @get:PropertyName("dueDate") @set:PropertyName("dueDate") var dueDate: String = "",
    @get:PropertyName("isPaid") @set:PropertyName("isPaid") var isPaid: Boolean = false
)

@Entity(
    tableName = "agreement_progress",
    foreignKeys = [
        ForeignKey(
            entity = AgreementEntity::class,
            parentColumns = ["id"],
            childColumns = ["agreementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("agreementId")]
)
data class AgreementProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agreementId: Long = 0,
    val apartment: String = "",
    val date: String = "",
    val description: String = ""
)

data class AgreementWithInstallments(
    @Embedded val agreement: AgreementEntity,
    @Relation(parentColumn = "id", entityColumn = "agreementId")
    val installments: List<InstallmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "agreementId")
    val progress: List<AgreementProgressEntity> = emptyList()
)
