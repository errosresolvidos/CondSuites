package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "lawsuits")
data class LawsuitEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("processNumber") @set:PropertyName("processNumber") var processNumber: String = "",
    @get:PropertyName("forum") @set:PropertyName("forum") var forum: String = "",
    @get:PropertyName("totalDebt") @set:PropertyName("totalDebt") var totalDebt: Double = 0.0,
    @get:PropertyName("registrationDate") @set:PropertyName("registrationDate") var registrationDate: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "Em Andamento",
    @get:PropertyName("successValue") @set:PropertyName("successValue") var successValue: Double? = null,
    @get:PropertyName("isFinished") @set:PropertyName("isFinished") var isFinished: Boolean = false
)

@Entity(
    tableName = "lawsuit_progress",
    foreignKeys = [
        ForeignKey(
            entity = LawsuitEntity::class,
            parentColumns = ["id"],
            childColumns = ["lawsuitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lawsuitId")]
)
data class LawsuitProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lawsuitId: Long = 0,
    val apartment: String = "",
    val date: String = "",
    val description: String = ""
)

data class LawsuitWithProgress(
    @Embedded val lawsuit: LawsuitEntity,
    @Relation(parentColumn = "id", entityColumn = "lawsuitId")
    val progress: List<LawsuitProgressEntity>
)
