package com.example.condsuites.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "PORTEIRO"
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("floor") @set:PropertyName("floor") var floor: Int = 2,
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") var phone: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String = ""
)

@Entity(tableName = "overtime")
data class OvertimeEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("employeeName") @set:PropertyName("employeeName") var employeeName: String = "",
    @get:PropertyName("employeeRole") @set:PropertyName("employeeRole") var employeeRole: String = "Zelador",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("startTime") @set:PropertyName("startTime") var startTime: String = "",
    @get:PropertyName("endTime") @set:PropertyName("endTime") var endTime: String = "",
    @get:PropertyName("totalHours") @set:PropertyName("totalHours") var totalHours: Double = 0.0,
    @get:PropertyName("reason") @set:PropertyName("reason") var reason: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "PENDENTE",
    @get:PropertyName("approvedByUsername") @set:PropertyName("approvedByUsername") var approvedByUsername: String = "",
    @get:PropertyName("registrationDate") @set:PropertyName("registrationDate") var registrationDate: String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
)

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String = "",
    val serviceType: String = "",
    val category: String = "",
    val monthlyValue: Double = 0.0,
    val contactName: String = "",
    val phone: String = "",
    val email: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = ""
)

@Entity(tableName = "delinquency_history")
data class DelinquencyHistoryEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("eventType") @set:PropertyName("eventType") var eventType: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("value") @set:PropertyName("value") var value: Double = 0.0
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val message: String = "",
    val status: String = "",
    val timestamp: String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
)

@Entity(tableName = "config_floors")
data class FloorEntity(@PrimaryKey val floor: String)

@Entity(tableName = "config_service_descriptions")
data class ServiceDescriptionEntity(@PrimaryKey val description: String)

@Entity(tableName = "config_process_statuses")
data class ProcessStatusEntity(@PrimaryKey val status: String)

@Entity(tableName = "config_occurrence_types")
data class OccurrenceTypeEntity(@PrimaryKey val type: String)
