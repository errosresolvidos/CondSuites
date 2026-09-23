package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "delinquents")
data class DelinquentEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("totalDebt") @set:PropertyName("totalDebt") var totalDebt: Double = 0.0,
    @get:PropertyName("registrationDate") @set:PropertyName("registrationDate") var registrationDate: String = "",
    @get:PropertyName("notification1Date") @set:PropertyName("notification1Date") var notification1Date: String? = null,
    @get:PropertyName("notification2Date") @set:PropertyName("notification2Date") var notification2Date: String? = null,
    @get:PropertyName("notification3Date") @set:PropertyName("notification3Date") var notification3Date: String? = null,
    @get:PropertyName("hasMadeAgreement") @set:PropertyName("hasMadeAgreement") var hasMadeAgreement: Boolean = false,
    @get:PropertyName("isArchived") @set:PropertyName("isArchived") var isArchived: Boolean = false
)

@Entity(
    tableName = "delinquent_progress",
    foreignKeys = [
        ForeignKey(
            entity = DelinquentEntity::class,
            parentColumns = ["id"],
            childColumns = ["delinquentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("delinquentId")]
)
data class DelinquentProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val delinquentId: Long = 0,
    val apartment: String = "",
    val date: String = "",
    val description: String = ""
)

data class DelinquentWithProgress(
    @Embedded val delinquent: DelinquentEntity,
    @Relation(parentColumn = "id", entityColumn = "delinquentId")
    val progress: List<DelinquentProgressEntity>
)
