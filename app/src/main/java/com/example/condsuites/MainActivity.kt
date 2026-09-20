package com.example.condsuites

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.condsuites.ui.theme.CondSuitesTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CondSuitesTheme {
                CondSuitesApp()
            }
        }
    }
}

// --- Room Database Configuration ---

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
    val delinquentId: Long,
    val apartment: String = "",
    val date: String,
    val description: String
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
    val agreementId: Long,
    val apartment: String = "",
    val date: String,
    val description: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "PORTEIRO"
)

@Entity(tableName = "occurrences")
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "ABERTA", // ABERTA, EM_ESPERA, FINALIZADA
    @get:PropertyName("createdByUsername") @set:PropertyName("createdByUsername") var createdByUsername: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "GERAL", // GERAL, CONSELHO
    @get:PropertyName("occurrenceType") @set:PropertyName("occurrenceType") var occurrenceType: String = "", // Reclamação, Sugestão, etc.
    @get:PropertyName("isUrgent") @set:PropertyName("isUrgent") var isUrgent: Boolean = false
)

@Entity(
    tableName = "occurrence_messages",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["occurrenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("occurrenceId")]
)
data class OccurrenceMessageEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("occurrenceId") @set:PropertyName("occurrenceId") var occurrenceId: Long = 0,
    @get:PropertyName("senderUsername") @set:PropertyName("senderUsername") var senderUsername: String = "",
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("isCouncilOnly") @set:PropertyName("isCouncilOnly") var isCouncilOnly: Boolean = false,
    @get:PropertyName("isSindicoOnly") @set:PropertyName("isSindicoOnly") var isSindicoOnly: Boolean = false,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    @get:PropertyName("isVotingClosed") @set:PropertyName("isVotingClosed") var isVotingClosed: Boolean = false,
    @get:PropertyName("isBudget") @set:PropertyName("isBudget") var isBudget: Boolean = false
)

@Entity(
    tableName = "occurrence_attachments",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class OccurrenceAttachmentEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("messageId") @set:PropertyName("messageId") var messageId: Long = 0,
    @get:PropertyName("occurrenceId") @set:PropertyName("occurrenceId") var occurrenceId: Long = 0,
    @get:PropertyName("fileName") @set:PropertyName("fileName") var fileName: String = "",
    @get:PropertyName("filePath") @set:PropertyName("filePath") var filePath: String = ""
)

@Entity(
    tableName = "occurrence_attachment_votes",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceAttachmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["attachmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attachmentId")]
)
data class OccurrenceAttachmentVoteEntity(
    @PrimaryKey(autoGenerate = true) @get:PropertyName("id") @set:PropertyName("id") var id: Long = 0,
    @get:PropertyName("attachmentId") @set:PropertyName("attachmentId") var attachmentId: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = ""
)

data class AttachmentWithVotes(
    @Embedded val attachment: OccurrenceAttachmentEntity,
    @Relation(parentColumn = "id", entityColumn = "attachmentId")
    val votes: List<OccurrenceAttachmentVoteEntity>
)

data class MessageWithAttachments(
    @Embedded val message: OccurrenceMessageEntity,
    @Relation(
        entity = OccurrenceAttachmentEntity::class,
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val attachments: List<AttachmentWithVotes>
)

data class OccurrenceWithMessages(
    @Embedded val occurrence: OccurrenceEntity,
    @Relation(
        entity = OccurrenceMessageEntity::class,
        parentColumn = "id",
        entityColumn = "occurrenceId"
    )
    val messages: List<MessageWithAttachments>
)

@Entity(tableName = "occurrence_logs")
data class OccurrenceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurrenceId: Long,
    val username: String,
    val action: String, // CRIOU, RESPONDEU
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val status: String,
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

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val validity: String = "",
    val contentText: String = "",
    val date: String = "",
    val authorUsername: String = ""
)

@Entity(tableName = "delinquency_history")
data class DelinquencyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val apartment: String = "",
    val ownerName: String = "",
    val eventType: String = "", // "Notificação", "Acordo", "Processo"
    val description: String = "",
    val date: String = "",
    val value: Double = 0.0
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

fun calculateOvertimeHours(startStr: String, endStr: String): Double {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.parse(startStr) ?: return 0.0
        val end = sdf.parse(endStr) ?: return 0.0
        var diff = end.time - start.time
        if (diff < 0) diff += 24 * 60 * 60 * 1000
        val hours = diff.toDouble() / (1000 * 60 * 60)
        java.math.BigDecimal(hours).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    } catch (_: Exception) {
        0.0
    }
}

fun getUnitIdForApartment(apt: String): Long {
    val clean = apt.trim()
    if (clean.isBlank()) return 1L
    val numOnly = clean.filter { it.isDigit() }.toLongOrNull()
    return if (numOnly != null && numOnly > 0) {
        numOnly
    } else {
        (clean.hashCode().toLong() and 0x7FFFFFFF)
    }
}

suspend fun deduplicateUnitsInDatabase(dao: AppDao) {
    try {
        val allUnits = dao.getAllUnitsList()
        if (allUnits.isEmpty()) return

        val grouped = allUnits.groupBy { it.apartment.trim().lowercase() }
        for ((_, list) in grouped) {
            if (list.size > 1) {
                val best = list.maxByOrNull {
                    (if (it.ownerName.isNotBlank()) 10 else 0) +
                    (if (it.phone.isNotBlank()) 5 else 0) +
                    (if (it.email.isNotBlank()) 5 else 0)
                } ?: list.first()

                val targetId = getUnitIdForApartment(best.apartment)
                val finalUnit = best.copy(id = targetId)

                list.forEach { dao.deleteUnit(it.id) }
                dao.insertUnit(finalUnit)
                FirestoreSyncManager.syncUnit(finalUnit)
            } else if (list.size == 1) {
                val unit = list.first()
                val targetId = getUnitIdForApartment(unit.apartment)
                if (unit.id != targetId) {
                    dao.deleteUnit(unit.id)
                    val updated = unit.copy(id = targetId)
                    dao.insertUnit(updated)
                    FirestoreSyncManager.syncUnit(updated)
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DEDUP_UNITS", "Error deduplicating units: ${e.message}")
    }
}

data class AgreementWithInstallments(
    @Embedded val agreement: AgreementEntity,
    @Relation(parentColumn = "id", entityColumn = "agreementId")
    val installments: List<InstallmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "agreementId")
    val progress: List<AgreementProgressEntity> = emptyList()
)

data class DelinquentWithProgress(
    @Embedded val delinquent: DelinquentEntity,
    @Relation(parentColumn = "id", entityColumn = "delinquentId")
    val progress: List<DelinquentProgressEntity>
)

data class ServiceOrderWithInstallments(
    @Embedded val order: ServiceOrderEntity,
    @Relation(parentColumn = "id", entityColumn = "serviceOrderId")
    val installments: List<ServiceInstallmentEntity>
)

data class LawsuitWithProgress(
    @Embedded val lawsuit: LawsuitEntity,
    @Relation(parentColumn = "id", entityColumn = "lawsuitId")
    val progress: List<LawsuitProgressEntity>
)

@Dao
interface AppDao {
    @Transaction @Query("SELECT * FROM agreements WHERE isArchived = 0")
    fun getActiveAgreements(): Flow<List<AgreementWithInstallments>>
    @Transaction @Query("SELECT * FROM agreements WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedAgreements(): Flow<List<AgreementWithInstallments>>
    @Transaction @Query("SELECT * FROM agreements WHERE apartment = :apt AND isArchived = 1")
    fun getAgreementHistory(apt: String): Flow<List<AgreementWithInstallments>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAgreement(agreement: AgreementEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertInstallments(installments: List<InstallmentEntity>)
    @Update suspend fun updateAgreement(agreement: AgreementEntity)
    @Update suspend fun updateInstallment(installment: InstallmentEntity)
    @Query("DELETE FROM agreements WHERE id = :id") suspend fun deleteAgreement(id: Long)

    @Transaction @Query("SELECT * FROM service_orders ORDER BY id DESC")
    fun getServiceOrders(): Flow<List<ServiceOrderWithInstallments>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertServiceOrder(order: ServiceOrderEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertServiceInstallments(installments: List<ServiceInstallmentEntity>)
    @Query("DELETE FROM service_orders WHERE id = :id") suspend fun deleteServiceOrder(id: Long)

    @Transaction @Query("SELECT * FROM delinquents WHERE isArchived = 0 ORDER BY id DESC")
    fun getDelinquents(): Flow<List<DelinquentWithProgress>>
    @Query("SELECT * FROM delinquents WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedDelinquents(): Flow<List<DelinquentWithProgress>>
    @Query("SELECT * FROM delinquents WHERE apartment = :apt LIMIT 1")
    suspend fun getDelinquentByApartment(apt: String): DelinquentEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertDelinquent(delinquent: DelinquentEntity)
    @Update suspend fun updateDelinquent(delinquent: DelinquentEntity)
    @Query("DELETE FROM delinquents WHERE id = :id") suspend fun deleteDelinquent(id: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertDelinquentProgress(progress: DelinquentProgressEntity)
    @Update suspend fun updateDelinquentProgress(progress: DelinquentProgressEntity)
    @Query("DELETE FROM delinquent_progress WHERE id = :id") suspend fun deleteDelinquentProgress(id: Long)

    @Transaction @Query("SELECT * FROM lawsuits ORDER BY id DESC")
    fun getLawsuitsWithProgress(): Flow<List<LawsuitWithProgress>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLawsuit(lawsuit: LawsuitEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLawsuitProgress(progress: LawsuitProgressEntity)
    @Update suspend fun updateLawsuit(lawsuit: LawsuitEntity)
    @Query("DELETE FROM lawsuits WHERE id = :id") suspend fun deleteLawsuit(id: Long)
    @Query("DELETE FROM lawsuit_progress WHERE id = :id") suspend fun deleteLawsuitProgress(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAgreementProgress(progress: AgreementProgressEntity)
    @Update suspend fun updateAgreementProgress(progress: AgreementProgressEntity)
    @Query("DELETE FROM agreement_progress WHERE id = :id") suspend fun deleteAgreementProgress(id: Long)

    @Query("SELECT * FROM delinquency_history ORDER BY id DESC")
    fun getDelinquencyHistory(): Flow<List<DelinquencyHistoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHistory(history: DelinquencyHistoryEntity)
    @Query("DELETE FROM delinquency_history WHERE id = :id") suspend fun deleteHistory(id: Long)

    @Query("SELECT * FROM config_floors ORDER BY floor ASC") fun getFloors(): Flow<List<FloorEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFloor(floor: FloorEntity)
    @Query("DELETE FROM config_floors WHERE floor = :floor") suspend fun deleteFloor(floor: String)

    @Query("SELECT * FROM config_service_descriptions ORDER BY description ASC") fun getServiceDescriptions(): Flow<List<ServiceDescriptionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertServiceDescription(desc: ServiceDescriptionEntity)
    @Query("DELETE FROM config_service_descriptions WHERE description = :desc") suspend fun deleteServiceDescription(desc: String)

    @Query("SELECT * FROM config_process_statuses ORDER BY status ASC") fun getProcessStatuses(): Flow<List<ProcessStatusEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProcessStatus(status: ProcessStatusEntity)
    @Query("DELETE FROM config_process_statuses WHERE status = :status") suspend fun deleteProcessStatus(status: String)

    @Query("SELECT * FROM config_occurrence_types ORDER BY type ASC") fun getOccurrenceTypes(): Flow<List<OccurrenceTypeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceType(type: OccurrenceTypeEntity)
    @Query("DELETE FROM config_occurrence_types WHERE type = :type") suspend fun deleteOccurrenceType(type: String)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") suspend fun getUserById(id: Long): UserEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUser(user: UserEntity)
    @Query("SELECT COUNT(*) FROM users") suspend fun getUserCount(): Int
    @Query("SELECT * FROM users ORDER BY id ASC") fun getAllUsersFlow(): Flow<List<UserEntity>>
    @Query("SELECT * FROM users ORDER BY id ASC") suspend fun getAllUsersList(): List<UserEntity>
    @Query("DELETE FROM users WHERE id = :id") suspend fun deleteUser(id: Long)

    @Transaction
    @Query("SELECT * FROM occurrences ORDER BY id DESC")
    fun getAllOccurrences(): Flow<List<OccurrenceWithMessages>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long
    @Query("SELECT * FROM occurrences WHERE id = :id") suspend fun getOccurrenceEntityById(id: Long): OccurrenceEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceReplace(occurrence: OccurrenceEntity): Long
    @Update suspend fun updateOccurrence(occurrence: OccurrenceEntity)
    @Query("DELETE FROM occurrences WHERE id = :id") suspend fun deleteOccurrence(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceMessage(message: OccurrenceMessageEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceMessageReplace(message: OccurrenceMessageEntity): Long
    @Update suspend fun updateOccurrenceMessage(message: OccurrenceMessageEntity)
    @Query("SELECT * FROM occurrence_messages") suspend fun getAllOccurrenceMessages(): List<OccurrenceMessageEntity>
    @Query("DELETE FROM occurrence_messages WHERE id = :id") suspend fun deleteOccurrenceMessage(id: Long)
    @Query("UPDATE occurrence_messages SET isVotingClosed = 1 WHERE id = :msgId") suspend fun closeOccurrenceVoting(msgId: Long)
    @Query("UPDATE occurrence_messages SET isRead = 1 WHERE occurrenceId = :occId AND senderUsername != :username")
    suspend fun markOccurrenceMessagesAsRead(occId: Long, username: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAttachmentReplace(attachment: OccurrenceAttachmentEntity): Long
    @Insert suspend fun insertAttachment(attachment: OccurrenceAttachmentEntity)
    @Query("SELECT * FROM occurrence_attachments") suspend fun getAllAttachments(): List<OccurrenceAttachmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAttachmentVote(vote: OccurrenceAttachmentVoteEntity)
    @Query("SELECT * FROM occurrence_attachment_votes") suspend fun getAllAttachmentVotes(): List<OccurrenceAttachmentVoteEntity>
    @Query("DELETE FROM occurrence_attachment_votes WHERE attachmentId = :attId AND username = :user")
    suspend fun deleteAttachmentVote(attId: Long, user: String)

    @Query("SELECT * FROM occurrence_logs ORDER BY timestamp DESC")
    fun getAllOccurrenceLogs(): Flow<List<OccurrenceLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceLog(log: OccurrenceLogEntity)

    @Query("DELETE FROM agreements") suspend fun clearAgreements()
    @Query("DELETE FROM delinquents") suspend fun clearNotifications()
    @Query("DELETE FROM lawsuits") suspend fun clearLawsuits()
    @Query("DELETE FROM service_orders") suspend fun clearServiceOrders()
    @Query("DELETE FROM delinquency_history") suspend fun clearHistory()
    @Query("DELETE FROM occurrences") suspend fun clearOccurrences()
    @Query("DELETE FROM contracts") suspend fun clearContracts()
    @Query("DELETE FROM users WHERE username != 'admin'") suspend fun clearUsers()
    @Query("DELETE FROM config_floors") suspend fun clearFloors()
    @Query("DELETE FROM config_service_descriptions") suspend fun clearServiceDescriptions()
    @Query("DELETE FROM config_process_statuses") suspend fun clearProcessStatuses()
    @Query("DELETE FROM config_occurrence_types") suspend fun clearOccurrenceTypes()

    @Query("SELECT * FROM agreements") suspend fun getAllAgreements(): List<AgreementEntity>
    @Query("SELECT * FROM installments") suspend fun getAllInstallments(): List<InstallmentEntity>
    @Query("SELECT * FROM service_orders") suspend fun getAllServiceOrders(): List<ServiceOrderEntity>
    @Query("SELECT * FROM service_installments") suspend fun getAllServiceInstallments(): List<ServiceInstallmentEntity>
    @Query("SELECT * FROM lawsuits") suspend fun getAllLawsuits(): List<LawsuitEntity>
    @Query("SELECT * FROM lawsuit_progress") suspend fun getAllLawsuitProgress(): List<LawsuitProgressEntity>
    @Query("SELECT * FROM delinquent_progress") suspend fun getAllDelinquentProgress(): List<DelinquentProgressEntity>
    @Query("SELECT * FROM agreement_progress") suspend fun getAllAgreementProgress(): List<AgreementProgressEntity>
    @Query("SELECT * FROM delinquents") suspend fun getAllDelinquents(): List<DelinquentEntity>
    @Query("SELECT * FROM delinquency_history") suspend fun getAllHistory(): List<DelinquencyHistoryEntity>
    @Query("SELECT * FROM contracts ORDER BY id DESC") fun getContracts(): Flow<List<ContractEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertContract(contract: ContractEntity)
    @Update suspend fun updateContract(contract: ContractEntity)
    @Query("DELETE FROM contracts WHERE id = :id") suspend fun deleteContract(id: Long)
    @Query("SELECT * FROM contracts") suspend fun getAllContracts(): List<ContractEntity>
    @Query("SELECT * FROM notification_logs ORDER BY id DESC") fun getNotificationLogs(): Flow<List<NotificationLogEntity>>
    @Insert suspend fun insertNotificationLog(log: NotificationLogEntity)
    @Query("DELETE FROM notification_logs") suspend fun clearNotificationLogs()

    @Query("SELECT * FROM units ORDER BY floor ASC, apartment ASC") fun getAllUnits(): Flow<List<UnitEntity>>
    @Query("SELECT * FROM units") suspend fun getAllUnitsList(): List<UnitEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUnit(unit: UnitEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertUnits(units: List<UnitEntity>)
    @Update suspend fun updateUnit(unit: UnitEntity)
    @Query("DELETE FROM units WHERE id = :id") suspend fun deleteUnit(id: Long)
    @Query("SELECT COUNT(*) FROM units") suspend fun getUnitCount(): Int

    @Query("SELECT * FROM overtime ORDER BY id DESC") fun getAllOvertimeFlow(): Flow<List<OvertimeEntity>>
    @Query("SELECT * FROM overtime ORDER BY id DESC") suspend fun getAllOvertimeList(): List<OvertimeEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOvertime(overtime: OvertimeEntity)
    @Update suspend fun updateOvertime(overtime: OvertimeEntity)
    @Query("DELETE FROM overtime WHERE id = :id") suspend fun deleteOvertime(id: Long)
}

@Database(entities = [AgreementEntity::class, InstallmentEntity::class, ServiceOrderEntity::class, ServiceInstallmentEntity::class, DelinquentEntity::class, LawsuitEntity::class, DelinquencyHistoryEntity::class, LawsuitProgressEntity::class, DelinquentProgressEntity::class, AgreementProgressEntity::class, UserEntity::class, OccurrenceEntity::class, OccurrenceLogEntity::class, OccurrenceMessageEntity::class, FloorEntity::class, ServiceDescriptionEntity::class, ProcessStatusEntity::class, OccurrenceTypeEntity::class, OccurrenceAttachmentEntity::class, OccurrenceAttachmentVoteEntity::class, ContractEntity::class, NotificationLogEntity::class, UnitEntity::class, OvertimeEntity::class], version = 38, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "condsuites_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private const val PREFS_NAME = "session_prefs"
    private const val KEY_CURRENT_USER = "current_session_username"

    fun saveCurrentSessionUser(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_USER, username).apply()
    }

    fun getCurrentSessionUser(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_USER, "") ?: ""
    }

    private val REMEMBERED_USERNAMES = stringSetPreferencesKey("remembered_usernames")

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current + username
        }
    }

    fun getRememberedUsernames(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[REMEMBERED_USERNAMES] ?: emptySet()
        }
    }

    suspend fun removeUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current - username
        }
    }
}

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
        val df = DecimalFormat("#,##0.00", symbols)
        val cleanString = text.text.replace(Regex("\\D"), "")
        if (cleanString.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        val parsed = cleanString.toDouble() / 100
        val formatted = df.format(parsed)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = text.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

class ProcessNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Formato: 0000000-00.0000.0.00.0000
        val cleanString = text.text.replace(Regex("\\D"), "").take(20)
        val out = StringBuilder()
        for (i in cleanString.indices) {
            out.append(cleanString[i])
            when (i) {
                6 -> out.append("-")
                8, 12, 13, 15 -> out.append(".")
            }
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 6) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                if (offset <= 13) return offset + 3
                if (offset <= 15) return offset + 4
                if (offset <= 20) return offset + 5
                return 25
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 16) return offset - 3
                if (offset <= 19) return offset - 4
                if (offset <= 25) return offset - 5
                return 20
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

fun calculateMonthlyDates(startDateStr: String, count: Int): List<String> {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dates = mutableListOf<String>()
    try {
        val startDate = sdf.parse(startDateStr) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        repeat(count) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }
    } catch (_: Exception) {
        val calendar = Calendar.getInstance()
        repeat(count) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }
    }
    return dates
}

fun formatCurrency(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(value)
}

fun calculateDaysDelay(dueDateStr: String): Long {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val dueDate = sdf.parse(dueDateStr) ?: return 0
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        if (today.after(dueDate)) TimeUnit.DAYS.convert(today.time - dueDate.time, TimeUnit.MILLISECONDS) else 0
    } catch (_: Exception) { 0 }
}

fun isMessageEditable(dateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: return false
        val diff = System.currentTimeMillis() - date.time
        diff < 5 * 60 * 1000 // 5 minutes
    } catch (_: Exception) {
        false
    }
}

fun isOvertimeEditable(registrationDateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return try {
        val date = sdf.parse(registrationDateStr) ?: return false
        val diff = System.currentTimeMillis() - date.time
        diff >= 0 && diff < 15 * 60 * 1000 // 15 minutos
    } catch (_: Exception) {
        false
    }
}

fun naturalSortApartments(apartment: String): String {
    return apartment.replace(Regex("(\\d+)")) { it.value.padStart(10, '0') }
}

fun shareAgreementReport(context: Context, agreement: AgreementWithInstallments, isDetailed: Boolean) {
    val report = StringBuilder()
    val typeName = if (isDetailed) "DETALHADO" else "RESUMIDO"
    report.append("📄 RELATÓRIO DE ACORDO ($typeName) - CONDSUITES\n==========================================\n\n")
    report.append("🏢 UNIDADE: ${agreement.agreement.apartment}\n👤 PROPRIETÁRIO: ${agreement.agreement.ownerName}\n")
    if (agreement.agreement.isLawsuit) {
        report.append("⚖️ STATUS: AJUIZADO\n")
    }
    if (agreement.agreement.n1Date != null || agreement.agreement.n2Date != null || agreement.agreement.n3Date != null) {
        report.append("📅 NOTIFICAÇÕES PRÉVIAS:\n")
        agreement.agreement.n1Date?.let { report.append("  - 1ª: $it\n") }
        agreement.agreement.n2Date?.let { report.append("  - 2ª: $it\n") }
        agreement.agreement.n3Date?.let { report.append("  - 3ª: $it\n") }
    }
    report.append("💰 DÍVIDA: R$ ${formatCurrency(agreement.agreement.totalDebt)}\n")
    val paid = agreement.installments.count { it.isPaid }
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    report.append("✅ PAGAS: $paid/${agreement.agreement.installmentsCount}\n🔻 RESTANTE: R$ ${formatCurrency(remaining)}\n")
    if (isDetailed) {
        report.append("\n📋 PARCELAS:\n")
        agreement.installments.sortedBy { it.number }.forEach { inst ->
            report.append("${inst.number}/${agreement.agreement.installmentsCount} | ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | ${if (inst.isPaid) "PAGO" else "ABERTO"}\n")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun generateElegantAgreementReportText(agreement: AgreementWithInstallments): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📄 COMPROVANTE / EXTRATO DE ACORDO\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("--------------------------------------------------\n")
    report.append("🚪 UNIDADE / APARTAMENTO: ${agreement.agreement.apartment}\n")
    report.append("👤 PROPRIETÁRIO: ${agreement.agreement.ownerName.uppercase()}\n")
    report.append("📝 Data do Acordo: ${agreement.agreement.date}\n")
    if (agreement.agreement.quotaMonths.isNotBlank()) {
        report.append("📋 Cotas Condominiais Acordadas:\n   ${agreement.agreement.quotaMonths.replace(";", ", ")}\n")
    }
    if (agreement.agreement.isLawsuit) {
        report.append("⚖️ Status Jurídico: 🚨 AJUIZADO\n")
    }
    report.append("--------------------------------------------------\n")
    report.append("💰 DÍVIDA TOTAL ACORDADA: R$ ${formatCurrency(agreement.agreement.totalDebt)}\n")
    val paid = agreement.installments.count { it.isPaid }
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    report.append("✅ Parcelas Pagas: $paid / ${agreement.agreement.installmentsCount}\n")
    report.append("🔻 Saldo Restante: R$ ${formatCurrency(remaining)}\n")
    report.append("--------------------------------------------------\n")
    report.append("📋 CRONOGRAMA DE PARCELAS:\n")
    agreement.installments.sortedBy { it.number }.forEach { inst ->
        val status = if (inst.isPaid) "✅ PAGO" else "⏳ ABERTO"
        report.append(" • Parcela ${inst.number}/${agreement.agreement.installmentsCount} | Venc: ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | $status\n")
    }
    report.append("\n════════════════════════════════════════\n")
    report.append(" Assinatura do Condômino: ____________________\n\n")
    report.append(" Assinatura da Administração: _________________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun generateElegantGeneralAgreementsReportText(agreements: List<AgreementWithInstallments>): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📊 RELATÓRIO GERAL DE ACORDOS\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val totalAgreed = agreements.sumOf { it.agreement.totalDebt }
    val totalRemaining = agreements.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    report.append("📋 Total de Acordos Ativos: ${agreements.size}\n")
    report.append("💰 Valor Total Acordado: R$ ${formatCurrency(totalAgreed)}\n")
    report.append("🔻 Valor Total Pendente: R$ ${formatCurrency(totalRemaining)}\n")
    report.append("--------------------------------------------------\n\n")

    agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
        val paidCount = item.installments.count { it.isPaid }
        val rem = item.installments.filter { !it.isPaid }.sumOf { it.value }
        report.append("[${idx + 1}] Apto ${item.agreement.apartment} - ${item.agreement.ownerName.uppercase()}\n")
        report.append("    Dívida: R$ ${formatCurrency(item.agreement.totalDebt)} | Pago: $paidCount/${item.agreement.installmentsCount} | Restante: R$ ${formatCurrency(rem)}\n")
        if (item.agreement.quotaMonths.isNotBlank()) {
            report.append("    Cotas: ${item.agreement.quotaMonths.replace(";", ", ")}\n")
        }
        report.append("    ----------------------------------------------\n")
    }

    report.append("\n════════════════════════════════════════\n")
    report.append(" Responsável Financeiro: ____________________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun generateElegantOccurrenceReportText(item: OccurrenceWithMessages): String {
    val occ = item.occurrence
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📋 RELATÓRIO DE OCORRÊNCIA\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("--------------------------------------------------\n")
    report.append("🚪 UNIDADE / LOCAL: ${occ.apartment}\n")
    report.append("👤 CRIADO POR: ${occ.createdByUsername.uppercase()}\n")
    report.append("📌 ASSUNTO / TÍTULO: ${occ.title}\n")
    report.append("📅 Data da Ocorrência: ${occ.date}\n")
    report.append("📊 Status: ${occ.status}\n")
    report.append("🚨 Urgência: ${if (occ.isUrgent) "⚡ URGENTE" else "Normal"}\n")
    report.append("--------------------------------------------------\n")
    report.append("💬 MENSAGENS E INTERAÇÕES:\n")
    if (item.messages.isEmpty()) {
        report.append("   Nenhuma mensagem registrada.\n")
    } else {
        item.messages.sortedBy { it.message.id }.forEach { msg ->
            report.append(" • [${msg.message.date}] ${msg.message.senderUsername.uppercase()}:\n   ${msg.message.text}\n\n")
        }
    }
    report.append("════════════════════════════════════════\n")
    report.append(" Responsável Técnico / Administração: _____\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun generateElegantGeneralOccurrencesReportText(title: String, occurrences: List<OccurrenceWithMessages>): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📊 $title\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("📋 Total de Ocorrências: ${occurrences.size}\n")
    report.append("--------------------------------------------------\n\n")

    occurrences.forEachIndexed { idx, item ->
        val occ = item.occurrence
        report.append("[${idx + 1}] Apto ${occ.apartment} - ${occ.title}\n")
        report.append("    Status: ${occ.status} | Urgência: ${if (occ.isUrgent) "URGENTE" else "Normal"} | Data: ${occ.date}\n")
        report.append("    Criado por: ${occ.createdByUsername} | Mensagens: ${item.messages.size}\n")
        report.append("    ----------------------------------------------\n")
    }

    report.append("\n════════════════════════════════════════\n")
    report.append(" Administração do Condomínio: ______________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun shareElegantAgreementReport(context: Context, agreement: AgreementWithInstallments) {
    val text = generateElegantAgreementReportText(agreement)
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Elegante de Acordo"))
}

fun shareElegantGeneralAgreementsReport(context: Context, agreements: List<AgreementWithInstallments>) {
    val text = generateElegantGeneralAgreementsReportText(agreements)
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Geral de Acordos"))
}

fun shareServiceOrderReport(context: Context, item: ServiceOrderWithInstallments, isDetailed: Boolean) {
    val report = StringBuilder()
    val typeName = if (isDetailed) "DETALHADO" else "RESUMIDO"
    report.append("📄 RELATÓRIO DE MANUTENÇÃO ($typeName) - ELEVADOR ${item.order.type.uppercase()}\n==========================================\n\n")
    report.append("🆔 OS: ${item.order.id}\n📅 DATA: ${item.order.date}\n📝 DESCRIÇÃO: ${item.order.description}\n🏢 ANDAR: ${item.order.floor}\n💰 VALOR: R$ ${formatCurrency(item.order.totalValue)}\n")
    if (isDetailed && item.order.isInstallment) {
        report.append("\n📋 DETALHAMENTO DE PAGAMENTO:\n")
        item.installments.sortedBy { it.number }.forEach { inst ->
            report.append("${inst.number}/${item.installments.size} | ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | ${if (inst.isPaid) "PAGO" else "ABERTO"}\n")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun shareLawsuitReport(context: Context, lawsuit: LawsuitEntity) {
    val report = StringBuilder()
    report.append("⚖️ RELATÓRIO JURÍDICO - CONDSUITES\n==========================================\n\n")
    report.append("🏢 UNIDADE: ${lawsuit.apartment}\n👤 PROPRIETÁRIO: ${lawsuit.ownerName}\n")
    report.append("📁 Nº PROCESSO: ${lawsuit.processNumber}\n🏛️ FÓRUM: ${lawsuit.forum}\n")
    report.append("💰 DÍVIDA AJUIZADA: R$ ${formatCurrency(lawsuit.totalDebt)}\n")
    report.append("📅 DATA AJUIZAMENTO: ${lawsuit.registrationDate}\n📊 STATUS: ${lawsuit.status}\n")
    val sv = lawsuit.successValue
    if (sv != null) {
        report.append("✅ VALOR DE ÊXITO: R$ ${formatCurrency(sv)}\n")
    }
    report.append("\n==========================================\n")
    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório Jurídico"))
}

fun shareDelinquencyHistoryReport(context: Context, historyList: List<DelinquencyHistoryEntity>, apartment: String? = null) {
    val report = StringBuilder()
    val title = if (apartment != null) "HISTÓRICO DE INADIMPLÊNCIA - APTO $apartment" else "RELATÓRIO GERAL DE INADIMPLÊNCIA"
    report.append("📋 $title\n==========================================\n\n")
    
    historyList.forEach { item ->
        report.append("📅 Data: ${item.date}\n")
        report.append("🏢 Unidade: ${item.apartment} | Proprietário: ${item.ownerName}\n")
        report.append("📌 Evento: ${item.eventType}\n")
        report.append("💰 Valor: R$ ${formatCurrency(item.value)}\n")
        report.append("------------------------------------------\n")
    }
    
    report.append("\nGerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Histórico"))
}

fun shareOccurrencesListReport(context: Context, title: String, items: List<OccurrenceWithMessages>) {
    val report = StringBuilder()
    report.append("📄 $title - CONDSUITES\n==========================================\n\n")
    if (items.isEmpty()) {
        report.append("Nenhuma ocorrência encontrada.\n")
    } else {
        items.forEach { item ->
            report.append("🆔 #${item.occurrence.id} | 🏢 ${item.occurrence.apartment}\n")
            report.append("📝 ${item.occurrence.title}\n")
            report.append("📅 Data: ${item.occurrence.date} | Status: ${item.occurrence.status}\n")
            if (item.occurrence.isUrgent) report.append("🚨 URGENTE\n")
            report.append("------------------------------------------\n")
        }
    }
    report.append("\nGerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun shareOccurrenceReport(context: Context, item: OccurrenceWithMessages) {
    val report = StringBuilder()
    report.append("📄 RELATÓRIO DE OCORRÊNCIA - CONDSUITES\n==========================================\n\n")
    report.append("🆔 OCORRÊNCIA: #${item.occurrence.id}\n")
    report.append("🏢 LOCAL: ${item.occurrence.apartment}\n")
    report.append("📝 ASSUNTO: ${item.occurrence.title}\n")
    report.append("📅 DATA: ${item.occurrence.date}\n")
    report.append("📊 STATUS: ${item.occurrence.status}\n")
    report.append("👤 CRIADO POR: ${item.occurrence.createdByUsername}\n")
    if (item.occurrence.isUrgent) {
        report.append("🚨 PRIORIDADE: URGENTE\n")
    }
    report.append("\n💬 CONVERSA:\n")
    item.messages.sortedBy { it.message.id }.forEach { msg ->
        report.append("------------------------------------------\n")
        report.append("[${msg.message.date}] ${msg.message.senderUsername}:\n")
        report.append("${msg.message.text}\n")
    }
    report.append("\n==========================================\n")
    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Ocorrência"))
}

sealed class Screen(val title: String, val icon: ImageVector, val route: String) {
    object Home : Screen("Início", Icons.Filled.Dashboard, "home")
    object Units : Screen("Unidades", Icons.Filled.Apartment, "units_parent")
    object UnitsRegistry : Screen("Cadastro", Icons.Filled.ContactPage, "units_registry")
    object Elevators : Screen("Elevadores", Icons.Filled.Elevator, "elevators")
    object Finance : Screen("Financeiro", Icons.Filled.AccountBalance, "finance")
    object Agreements : Screen("Acordos", Icons.Filled.Handshake, "agreements")
    object Lawsuits : Screen("Ajuizados", Icons.Filled.Gavel, "lawsuits")
    object Notices : Screen("Mural de Avisos", Icons.AutoMirrored.Filled.Announcement, "notices")
    object Settings : Screen("Configurações", Icons.Filled.Settings, "settings")
    object DelinquencyHistory : Screen("Histórico de Inadimplência", Icons.Filled.History, "del_hist")
    object Contracts : Screen("Contratos", Icons.Filled.Description, "contracts")
    object Maintenance : Screen("Manutenção", Icons.Filled.Handyman, "maint_parent")
    object Reports : Screen("Relatórios", Icons.AutoMirrored.Filled.ListAlt, "reports_parent")
    object ReportsAgreements : Screen("Relatório de Acordos", Icons.Filled.Handshake, "rep_agg")
    object ReportsLawsuits : Screen("Relatório de Ajuizados", Icons.Filled.Gavel, "rep_lawsuits")
    object ReportsElevOccurrences : Screen("Ocorrências Elevadores", Icons.Filled.Analytics, "rep_elev_occ")
    object ReportsOvertime : Screen("Relatório de Horas Extras", Icons.Filled.AccessTime, "rep_overtime")
    object Occurrences : Screen("Ocorrências", Icons.Default.Assignment, "occurrences_parent")
    object OccurrencesListNav : Screen("Ocorrências", Icons.Default.Assignment, "occurrences_list")
    object RegisterOccurrenceNav : Screen("Cadastrar Ocorrência", Icons.Default.Add, "register_occurrence_nav")
    object Overtime : Screen("Lançar Horas Extras", Icons.Filled.AccessTime, "overtime")
    object UserManagement : Screen("Gestão de Usuários", Icons.Filled.People, "users_mgmt")
}

data class NavigationItem(val screen: Screen, val subItems: List<NavigationItem> = emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CondSuitesApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.appDao()
    val scope = rememberCoroutineScope()
    
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            UserPreferences.saveCurrentSessionUser(context, currentUser!!.username)
        } else {
            UserPreferences.saveCurrentSessionUser(context, "")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("PERMISSIONS", "POST_NOTIFICATIONS granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        try {
            if (dao.getUserCount() == 0) {
                val admin = UserEntity(username = "admin", password = "admin", role = "ADMIN")
                dao.insertUser(admin)
                FirestoreSyncManager.syncUser(admin)
            }
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("occurrences")
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        android.util.Log.d("FCM_TOKEN", "Current FCM Token: $token")
                    }
                }
            } catch (_: Exception) {}

            FirestoreSyncManager.startListening(context.applicationContext, dao, scope)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (currentUser == null) {
        LoginScreen(dao) { user -> currentUser = user }
    } else {
        MainContent(
            currentUser = currentUser!!, 
            dao = dao, 
            scope = scope, 
            onLogout = { currentUser = null },
            onUserSwitched = { newUser -> currentUser = newUser }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    currentUser: UserEntity, 
    dao: AppDao, 
    scope: CoroutineScope, 
    onLogout: () -> Unit,
    onUserSwitched: (UserEntity) -> Unit
) {
    val context = LocalContext.current
    val canAccessFinance = currentUser.role != "Zelador" && currentUser.role != "Porteiro"
    val canAccessMaintenance = true
    
    val navItems = mutableListOf(
        NavigationItem(Screen.Home),
        NavigationItem(Screen.Units, listOf(
            NavigationItem(Screen.UnitsRegistry)
        )),
        NavigationItem(Screen.Occurrences, listOf(
            NavigationItem(Screen.OccurrencesListNav),
            NavigationItem(Screen.RegisterOccurrenceNav),
            NavigationItem(Screen.Overtime)
        )),
    )
    
    if (canAccessFinance) {
        navItems.add(NavigationItem(Screen.Finance, listOf(
            NavigationItem(Screen.Agreements), 
            NavigationItem(Screen.Lawsuits),
            NavigationItem(Screen.DelinquencyHistory),
            NavigationItem(Screen.Contracts)
        )))
    }

    val reportSubItems = mutableListOf<NavigationItem>()
    if (canAccessFinance) {
        reportSubItems.add(NavigationItem(Screen.ReportsAgreements))
        reportSubItems.add(NavigationItem(Screen.ReportsLawsuits))
    }
    if (canAccessMaintenance) {
        reportSubItems.add(NavigationItem(Screen.ReportsElevOccurrences))
    }
    reportSubItems.add(NavigationItem(Screen.ReportsOvertime))

    navItems.add(NavigationItem(Screen.Reports, reportSubItems))
    navItems.add(NavigationItem(Screen.Notices))

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var expandedItems by remember { mutableStateOf(setOf<String>()) }
    val activeAgreements by dao.getActiveAgreements().collectAsState(initial = emptyList())
    val delinquents by dao.getDelinquents().collectAsState(initial = emptyList())
    val archivedDelinquents by dao.getArchivedDelinquents().collectAsState(initial = emptyList())
    val archivedAgreements by dao.getArchivedAgreements().collectAsState(initial = emptyList())
    val lawsuitsState by dao.getLawsuitsWithProgress().collectAsState(initial = emptyList())

    var showArchivedNotifs by remember { mutableStateOf(false) }
    var showArchivedAgreements by remember { mutableStateOf(false) }
    var isOccurrenceCompactView by remember { mutableStateOf(true) }
    var isAgreementCompactView by remember { mutableStateOf(true) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<OccurrenceMessageEntity?>(null) }
    var showEditMessageDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    if (showSwitchUserDialog) {
        QuickSwitchUserDialog(
            dao = dao,
            onDismiss = { showSwitchUserDialog = false },
            onUserSelected = { newUser ->
                onUserSwitched(newUser)
                showSwitchUserDialog = false
            }
        )
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Apartment, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("CondSuites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Gestão Condominial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(currentUser.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            currentUser.role,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(navItems) { item -> 
                        RecursiveNavItem(
                            item = item, 
                            currentScreen = currentScreen, 
                            expandedItems = expandedItems, 
                            level = 0, 
                            onExpandToggle = { route -> expandedItems = if (expandedItems.contains(route)) expandedItems - route else expandedItems + route }, 
                            onScreenSelect = { screen -> currentScreen = screen; scope.launch { drawerState.close() } } 
                        ) 
                    }
                }

                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NavigationDrawerItem(
                        label = { Text("Trocar Usuário") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            showSwitchUserDialog = true
                        },
                        icon = { Icon(Icons.Default.SwitchAccount, null) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Configurações") },
                        selected = currentScreen is Screen.Settings,
                        onClick = { 
                            currentScreen = Screen.Settings
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, null) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Sair do Aplicativo") },
                        selected = false,
                        onClick = { showExitDialog = true },
                        icon = { Icon(Icons.Default.Logout, null, tint = Color.Red) },
                        colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = Color.Red),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = { 
                CenterAlignedTopAppBar(
                    title = { Text(currentScreen.title, fontWeight = FontWeight.Bold) }, 
                    navigationIcon = { IconButton({ scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) } },
                    actions = {
                        IconButton(onClick = { showSwitchUserDialog = true }) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = "Trocar Usuário")
                        }
                        if (currentScreen is Screen.Agreements) {
                            var showMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Notificações arquivadas") },
                                    onClick = { 
                                        showMenu = false
                                        showArchivedNotifs = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Archive, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Acordos arquivados") },
                                    onClick = { 
                                        showMenu = false
                                        showArchivedAgreements = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.History, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isAgreementCompactView) "Visualização Completa" else "Visualização Compacta") },
                                    onClick = { 
                                        isAgreementCompactView = !isAgreementCompactView
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(if (isAgreementCompactView) Icons.Default.ViewStream else Icons.Default.ViewAgenda, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Histórico de Inadimplente") },
                                    onClick = { 
                                        showMenu = false
                                        currentScreen = Screen.DelinquencyHistory
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }
                                )
                            }
                        }
                        
                        if (currentScreen is Screen.Occurrences) {
                            var showOccMenu by remember { mutableStateOf(false) }
                            IconButton(onClick = { showOccMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(expanded = showOccMenu, onDismissRequest = { showOccMenu = false }) {
                                if (selectedMessage != null && isMessageEditable(selectedMessage!!.date) && selectedMessage!!.senderUsername == currentUser.username) {
                                    DropdownMenuItem(
                                        text = { Text("Editar Mensagem") },
                                        onClick = { 
                                            showOccMenu = false
                                            showEditMessageDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(if (isOccurrenceCompactView) "Visualização Completa" else "Visualização Compacta") },
                                    onClick = { 
                                        isOccurrenceCompactView = !isOccurrenceCompactView
                                        showOccMenu = false
                                    },
                                    leadingIcon = { Icon(if (isOccurrenceCompactView) Icons.Default.ViewStream else Icons.Default.ViewAgenda, null) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) 
            }
        ) { inner ->
            Box(Modifier.padding(inner)) {
                when (currentScreen) {
                    is Screen.Home -> HomeScreen(dao, currentUser) { currentScreen = it }
                    is Screen.Units, is Screen.UnitsRegistry -> UnitsRegistryScreen(dao, context)
                    is Screen.Agreements -> if (canAccessFinance) AgreementsManagementScreen(
                        agreements = activeAgreements, 
                        delinquents = delinquents, 
                        lawsuits = lawsuitsState, 
                        dao = dao, 
                        scope = scope,
                        currentUser = currentUser,
                        isCompact = isAgreementCompactView
                    ) else PlaceholderScreen(currentScreen)
                    is Screen.Lawsuits -> if (canAccessFinance) LawsuitsManagementScreen(lawsuitsState, activeAgreements, dao, scope) else PlaceholderScreen(currentScreen)
                    is Screen.DelinquencyHistory -> if (canAccessFinance) DelinquencyHistoryScreen(dao, scope, currentUser) else PlaceholderScreen(currentScreen)
                    is Screen.Contracts -> if (canAccessFinance) ContractsScreen(dao, scope, currentUser) else PlaceholderScreen(currentScreen)
                    is Screen.Settings -> SettingsScreen(dao, currentUser, scope)
                    is Screen.ReportsAgreements -> if (canAccessFinance) ReportsAgreementsScreen(dao, context) else PlaceholderScreen(currentScreen)
                    is Screen.ReportsLawsuits -> if (canAccessFinance) ReportsLawsuitsScreen(dao, context) else PlaceholderScreen(currentScreen)
                    is Screen.ReportsElevOccurrences -> if (canAccessMaintenance) ReportsElevatorOccurrencesScreen(dao, context) else PlaceholderScreen(currentScreen)
                    is Screen.ReportsOvertime -> ReportsOvertimeScreen(dao, context)
                    is Screen.Overtime -> OvertimeManagementScreen(dao, currentUser, scope)
                    is Screen.Occurrences, is Screen.OccurrencesListNav -> OccurrencesScreen(
                        dao = dao, 
                        currentUser = currentUser, 
                        scope = scope, 
                        isCompact = isOccurrenceCompactView,
                        selectedMessage = selectedMessage,
                        onMessageSelected = { selectedMessage = it },
                        initialShowAddDialog = false
                    )
                    is Screen.RegisterOccurrenceNav -> OccurrencesScreen(
                        dao = dao, 
                        currentUser = currentUser, 
                        scope = scope, 
                        isCompact = isOccurrenceCompactView,
                        selectedMessage = selectedMessage,
                        onMessageSelected = { selectedMessage = it },
                        initialShowAddDialog = true
                    )
                    else -> PlaceholderScreen(currentScreen)
                }
                
                if (showArchivedNotifs) {
                    ArchivedNotificationsDialog(archivedDelinquents, { scope.launch { dao.deleteDelinquent(it.id) } }, { scope.launch { dao.updateDelinquent(it.copy(isArchived = false)) } }) {
                        showArchivedNotifs = false
                    }
                }

                if (showArchivedAgreements) {
                    ArchivedAgreementsDialog(archivedAgreements, { scope.launch { dao.deleteAgreement(it.agreement.id) } }, { scope.launch { dao.updateAgreement(it.agreement.copy(isArchived = false)) } }) {
                        showArchivedAgreements = false
                    }
                }

                if (showEditMessageDialog && selectedMessage != null) {
                    var editText by remember { mutableStateOf(selectedMessage!!.text) }
                    AlertDialog(
                        onDismissRequest = { showEditMessageDialog = false; selectedMessage = null },
                        title = { Text("Editar Mensagem") },
                        text = {
                            OutlinedTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                scope.launch {
                                    val updated = selectedMessage!!.copy(text = editText)
                                    dao.updateOccurrenceMessage(updated)
                                    FirestoreSyncManager.syncOccurrenceMessage(updated)
                                    showEditMessageDialog = false
                                    selectedMessage = null
                                }
                            }) { Text("Salvar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditMessageDialog = false; selectedMessage = null }) { Text("Cancelar") }
                        }
                    )
                }

                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Sair do Aplicativo") },
                        text = { Text("Deseja realmente fechar o aplicativo?") },
                        confirmButton = {
                            Button(onClick = { (context as? Activity)?.finish() }) {
                                Text("Sim")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) {
                                Text("Não")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OccurrencesScreen(
    dao: AppDao, 
    currentUser: UserEntity, 
    scope: CoroutineScope, 
    isCompact: Boolean = true,
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    initialShowAddDialog: Boolean = false
) {
    val context = LocalContext.current
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FirestoreSyncManager.pullAllFromCloud(dao, scope)
        }
    }
    
    val canSeeConselho = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = if (canSeeConselho) listOf("Zelador", "Conselho") else listOf("Zelador")
    
    var showAddDialog by remember { mutableStateOf(initialShowAddDialog) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val filteredOccurrences = occurrences.filter { 
        val isTargetConselho = it.occurrence.type == "CONSELHO"
        val isTargetZelador = it.occurrence.type == "GERAL"
        
        val matchesTab = if (selectedTab == 0) isTargetZelador else isTargetConselho
        
        val canAccess = when (currentUser.role) {
            "Zelador" -> isTargetZelador
            "Conselheiro Fiscal", "Síndico", "ADMIN" -> true
            else -> isTargetZelador // Porteiro etc see Zelador ones
        }
        
        val matchesSearch = it.occurrence.title.contains(searchQuery, ignoreCase = true) ||
                it.occurrence.apartment.contains(searchQuery, ignoreCase = true) ||
                it.messages.any { msg -> msg.message.text.contains(searchQuery, ignoreCase = true) }
        
        matchesTab && canAccess && (searchQuery.isEmpty() || matchesSearch)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedIds.size} selecionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            if (tabs.size > 1) {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar ocorrência") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (filteredOccurrences.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val message = if (selectedTab == 0) "Nenhuma ocorrência para o Zelador." else "Nenhuma pauta do Conselho."
                        Text(message, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredOccurrences) { occWithMsgs ->
                            val isSelected = selectedIds.contains(occWithMsgs.occurrence.id)
                            OccurrenceCard(
                                occWithMsgs = occWithMsgs, 
                                currentUser = currentUser, 
                                isCompact = isCompact,
                                isSelected = isSelected,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                            selectedIds = if (isSelected) selectedIds - occWithMsgs.occurrence.id else selectedIds + occWithMsgs.occurrence.id
                                        }
                                    },
                                    onLongClick = {
                                        if (currentUser.role == "ADMIN") {
                                            selectedIds = selectedIds + occWithMsgs.occurrence.id
                                        }
                                    }
                                ),
                                selectedMessage = selectedMessage,
                                onMessageSelected = onMessageSelected,
                                onReply = { text, isCouncilOnly, isSindicoOnly, uris, isBudget ->
                                    scope.launch {
                                        val msg = OccurrenceMessageEntity(
                                            occurrenceId = occWithMsgs.occurrence.id,
                                            senderUsername = currentUser.username,
                                            text = text,
                                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                                            isCouncilOnly = isCouncilOnly,
                                            isSindicoOnly = isSindicoOnly,
                                            isBudget = isBudget
                                        )
                                        val messageId = dao.insertOccurrenceMessage(msg)
                                        FirestoreSyncManager.syncOccurrenceMessage(msg.copy(id = messageId))
                                        
                                        val notifTitle = when {
                                            isSindicoOnly -> "Nova mensagem sigilosa (Síndico)"
                                            isCouncilOnly -> "Nova mensagem sigilosa (Conselho)"
                                            else -> "Nova mensagem em Ocorrências"
                                        }
                                        val notifBody = "${occWithMsgs.occurrence.title} - ${currentUser.username}: $text"
                                        sendFcmPushNotification(
                                            dao = dao, 
                                            title = notifTitle, 
                                            body = notifBody, 
                                            senderUsername = currentUser.username, 
                                            occurrenceId = occWithMsgs.occurrence.id
                                        )

                                        uris.forEach { uri ->
                                            uploadImageToCloudinary(context, uri) { path ->
                                                if (path != null) {
                                                    scope.launch {
                                                        val att = OccurrenceAttachmentEntity(
                                                            messageId = messageId,
                                                            occurrenceId = occWithMsgs.occurrence.id,
                                                            fileName = getFileName(context, uri),
                                                            filePath = path
                                                        )
                                                        val attId = dao.insertAttachmentReplace(att)
                                                        FirestoreSyncManager.syncAttachment(att.copy(id = attId))
                                                    }
                                                }
                                            }
                                        }

                                        val action = when {
                                            isSindicoOnly -> "REPLICOU_SIGILOSO_SINDICO"
                                            isCouncilOnly -> "REPLICOU_SIGILOSO_CONSELHO"
                                            else -> "REPLICOU"
                                        }
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = action))
                                    }
                                },
                                onUpdateStatus = { newStatus ->
                                    scope.launch {
                                        dao.updateOccurrence(occWithMsgs.occurrence.copy(status = newStatus))
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = "STATUS_$newStatus"))
                                    }
                                },
                                onMarkAsRead = {
                                    scope.launch {
                                        dao.markOccurrenceMessagesAsRead(occWithMsgs.occurrence.id, currentUser.username)
                                        cancelAppNotification(context, occWithMsgs.occurrence.id.toInt())
                                        val unreadMsgs = occWithMsgs.messages.map { it.message }.filter { !it.isRead && it.senderUsername != currentUser.username }
                                        unreadMsgs.forEach { msg ->
                                            FirestoreSyncManager.syncOccurrenceMessage(msg.copy(isRead = true))
                                        }
                                    }
                                },
                                onDeleteMessage = { msgId ->
                                    scope.launch { dao.deleteOccurrenceMessage(msgId) }
                                },
                                onToggleVote = { attId, hasVoted ->
                                    scope.launch {
                                        if (hasVoted) {
                                            dao.deleteAttachmentVote(attId, currentUser.username)
                                            FirestoreSyncManager.syncAttachmentVote(
                                                OccurrenceAttachmentVoteEntity(attachmentId = attId, username = currentUser.username),
                                                isDelete = true
                                            )
                                        } else {
                                            val vote = OccurrenceAttachmentVoteEntity(attachmentId = attId, username = currentUser.username)
                                            dao.insertAttachmentVote(vote)
                                            FirestoreSyncManager.syncAttachmentVote(vote)
                                        }
                                    }
                                },
                                onCloseVoting = { msgId, name, total, count, instValue, start, dur ->
                                    scope.launch {
                                        dao.closeOccurrenceVoting(msgId)
                                        if (name != null && total != null && count != null && instValue != null) {
                                            val resultText = StringBuilder().apply {
                                                append("Votação encerrada.\n\n")
                                                append("✅ Orçamento Aprovado: $name\n")
                                                append("💰 Valor Total: R$ ${formatCurrency(total)}\n")
                                                append("💳 Parcelas: $count x R$ ${formatCurrency(instValue)}\n")
                                                if (!start.isNullOrBlank()) append("📅 Início: $start\n")
                                                if (!dur.isNullOrBlank()) append("⏱️ Duração: $dur")
                                            }.toString()
                                            val msg = OccurrenceMessageEntity(
                                                occurrenceId = occWithMsgs.occurrence.id,
                                                senderUsername = "SISTEMA",
                                                text = resultText,
                                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                                                isCouncilOnly = false,
                                                isSindicoOnly = false
                                            )
                                            val sysMsgId = dao.insertOccurrenceMessage(msg)
                                            FirestoreSyncManager.syncOccurrenceMessage(msg.copy(id = sysMsgId))
                                            sendFcmPushNotification(
                                                dao = dao,
                                                title = "Votação Encerrada na Ocorrência",
                                                body = "${occWithMsgs.occurrence.title}: Votação encerrada no orçamento $name.",
                                                senderUsername = currentUser.username,
                                                occurrenceId = occWithMsgs.occurrence.id
                                            )
                                            dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = "VOTACAO_ENCERRADA"))
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch { dao.deleteOccurrence(occWithMsgs.occurrence.id) }
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { 
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) searchQuery = ""
                },
                containerColor = if (isSearchVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                contentColor = if (isSearchVisible) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onTertiary
            ) {
                Icon(if (isSearchVisible) Icons.Default.Close else Icons.Default.Search, contentDescription = "Buscar")
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Ocorrência")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja excluir as ${selectedIds.size} ocorrências selecionadas?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            selectedIds.forEach { id ->
                                dao.deleteOccurrence(id)
                            }
                            selectedIds = emptySet()
                            showDeleteConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showAddDialog) {
        RegisterOccurrenceDialog(currentUser, dao, onDismiss = { showAddDialog = false }) { title, desc, apt, isUrgent, uris, dest ->
            scope.launch {
                val newOcc = OccurrenceEntity(
                    title = title,
                    apartment = apt,
                    status = "ABERTA",
                    createdByUsername = currentUser.username,
                    date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    type = dest,
                    isUrgent = isUrgent
                )
                val id = dao.insertOccurrence(newOcc)
                val savedOcc = newOcc.copy(id = id)
                sendFcmPushNotification(
                    dao = dao, 
                    title = "Nova Ocorrência: $title", 
                    body = "Unidade $apt: $desc", 
                    senderUsername = currentUser.username, 
                    occurrenceId = id
                )
                FirestoreSyncManager.syncOccurrence(savedOcc)
                val firstMsg = OccurrenceMessageEntity(
                    occurrenceId = id,
                    senderUsername = currentUser.username,
                    text = desc,
                    date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                )
                val messageId = dao.insertOccurrenceMessage(firstMsg)
                FirestoreSyncManager.syncOccurrenceMessage(firstMsg.copy(id = messageId))
                
                uris.forEach { uri ->
                    uploadImageToCloudinary(context, uri) { path ->
                        if (path != null) {
                            scope.launch {
                                val att = OccurrenceAttachmentEntity(
                                    messageId = messageId,
                                    occurrenceId = id,
                                    fileName = getFileName(context, uri),
                                    filePath = path
                                )
                                val attId = dao.insertAttachmentReplace(att)
                                FirestoreSyncManager.syncAttachment(att.copy(id = attId))
                            }
                        }
                    }
                }
                
                dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = id, username = currentUser.username, action = "CRIOU"))
                if (dest == "CONSELHO") selectedTab = 1
                showAddDialog = false
            }
        }
    }
}

@Composable
fun OccurrenceCard(
    occWithMsgs: OccurrenceWithMessages, 
    currentUser: UserEntity, 
    isCompact: Boolean = true,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    onReply: (String, Boolean, Boolean, List<Uri>, Boolean) -> Unit, 
    onUpdateStatus: (String) -> Unit,
    onMarkAsRead: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onToggleVote: (Long, Boolean) -> Unit,
    onCloseVoting: (Long, String?, Double?, Int?, Double?, String?, String?) -> Unit,
    onDelete: () -> Unit
) {
    var showReplyDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(!isCompact) }
    
    // Always sync with isCompact changes from parent, but allow local toggle
    LaunchedEffect(isCompact) {
        expanded = !isCompact
    }

    val occ = occWithMsgs.occurrence
    val messages = occWithMsgs.messages.sortedBy { it.message.id }
    val isCondo = occ.apartment == "CONDOMÍNIO"

    LaunchedEffect(expanded, messages.size) {
        if (expanded) {
            onMarkAsRead()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded }, 
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD) // Muito azul claro para Condomínio
                else -> Color(0xFFF7F7F7) // Cinza padrão para Unidade
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            occ.title, 
                            fontWeight = FontWeight.ExtraBold, 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (occ.isUrgent) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color.Red,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "URGENTE",
                                    Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isCondo) Color(0xFFBBDEFB) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: ${occ.apartment}", 
                                Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCondo) Color(0xFF0D47A1) else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                Surface(
                    color = when(occ.status) {
                        "ABERTA" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                        "EM_ESPERA" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        else -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        occ.status.replace("_", " "), 
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                        color = when(occ.status) {
                            "ABERTA" -> Color(0xFF1976D2)
                            "EM_ESPERA" -> Color(0xFFEF6C00)
                            else -> Color(0xFF2E7D32)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                    tint = Color.Gray
                )
            }
            
            // Sub-header summary info (always visible in card header)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        Text(
                            "💬 ${messages.size} ${if (messages.size == 1) "mensagem" else "mensagens"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    val unreadCount = messages.count { !it.message.isRead && it.message.senderUsername != currentUser.username }
                    if (!expanded && unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "$unreadCount nova(s)",
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    occ.date, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                // Conversation (WhatsApp style chat bubbles)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    messages.forEach { msgWithAtts ->
                        val msg = msgWithAtts.message
                        
                        val canSeeSecret = when {
                            msg.isSindicoOnly -> currentUser.role == "Síndico" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            msg.isCouncilOnly -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            msg.isBudget -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            else -> true
                        }
                        
                        val isVotingResult = msg.senderUsername == "SISTEMA" && msg.text.startsWith("Votação encerrada.")
                        val canSeeVotingResult = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"

                        val displayMsg = if (!canSeeSecret) {
                            "Mensagem sigilosa"
                        } else if (isVotingResult && !canSeeVotingResult) {
                            "Resultado da votação restrito ao Conselho Fiscal e Síndico."
                        } else {
                            msg.text
                        }
                        
                        ChatBubble(
                            text = displayMsg,
                            author = msg.senderUsername,
                            date = msg.date,
                            isFromMe = msg.senderUsername == currentUser.username || (msg.senderUsername == "admin" && currentUser.role == "ADMIN"),
                            isConfidential = msg.isCouncilOnly,
                            isSindicoOnly = msg.isSindicoOnly,
                            isRead = msg.isRead,
                            isVotingClosed = msg.isVotingClosed,
                            isBudget = msg.isBudget,
                            attachments = msgWithAtts.attachments,
                            isSelected = selectedMessage?.id == msg.id,
                            onLongClick = { 
                                if (onMessageSelected != null) {
                                    if (selectedMessage?.id == msg.id) onMessageSelected(null)
                                    else onMessageSelected(msg)
                                }
                            },
                            currentUser = currentUser,
                            onToggleVote = onToggleVote,
                            onCloseVoting = { name, total, count, instValue, start, dur -> 
                                onCloseVoting(msg.id, name, total, count, instValue, start, dur) 
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Action Buttons
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val canReply = occ.status != "FINALIZADA" && currentUser.role != "Porteiro" && currentUser.role != "Zelador"
                    val canZeladorReply = occ.status != "FINALIZADA" && currentUser.role == "Zelador" && occ.type == "GERAL"
                    
                    if (canReply || canZeladorReply) {
                        TextButton(onClick = { showReplyDialog = true }) {
                            Icon(Icons.Default.Reply, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Responder")
                        }
                    }

                    if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.role == "Conselheiro Fiscal") {
                        if (occ.status != "FINALIZADA") {
                            if (currentUser.role == "Síndico" || currentUser.role == "ADMIN") {
                                ProfessionalBudgetActionButton(onClick = { showBudgetDialog = true })
                                Spacer(Modifier.width(6.dp))
                            }
                            
                            TextButton(onClick = { onUpdateStatus("FINALIZADA") }) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Fechar")
                            }
                        } else {
                            TextButton(onClick = { onUpdateStatus("ABERTA") }) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reabrir")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog) {
        var replyText by remember { mutableStateOf("") }
        var isCouncilOnly by remember { mutableStateOf(false) }
        var isSindicoOnly by remember { mutableStateOf(false) }
        val selectedUris = remember { mutableStateListOf<Uri>() }
        val context = LocalContext.current
        
        val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            selectedUris.addAll(uris)
        }
        
        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("Continuar Conversa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(replyText, { replyText = it }, label = { Text("Sua mensagem") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    
                    if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || (currentUser.role == "Conselheiro Fiscal" && occ.type == "CONSELHO")) {
                        Column {
                            if (currentUser.role == "Síndico" || currentUser.role == "ADMIN") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isCouncilOnly, onCheckedChange = { isCouncilOnly = it; if(it) isSindicoOnly = false })
                                    Text("Sigiloso (Apenas Conselho)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            if (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isSindicoOnly, onCheckedChange = { isSindicoOnly = it; if(it) isCouncilOnly = false })
                                    Text("Sigiloso (Apenas Síndico)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Anexar Documentos")
                    }
                    
                    if (selectedUris.isNotEmpty()) {
                        Text("Anexos (${selectedUris.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column {
                            selectedUris.forEach { uri ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(Modifier.width(4.dp))
                                    Text(getFileName(context, uri), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    if (replyText.isNotBlank() || selectedUris.isNotEmpty()) { 
                        onReply(replyText, isCouncilOnly, isSindicoOnly, selectedUris.toList(), false)
                        showReplyDialog = false 
                    } 
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showBudgetDialog) {
        BudgetReplyDialog(
            onDismiss = { showBudgetDialog = false },
            onConfirm = { text, uris ->
                onReply(text, true, false, uris, true)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
fun ProfessionalBudgetBadgeIcon(
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    size: Dp = 36.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(10.dp),
        color = badgeColor,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Orçamento",
                tint = iconColor,
                modifier = Modifier.size(size * 0.58f)
            )
        }
    }
}

@Composable
fun ProfessionalBudgetActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Orçamento",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Orçamento",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BudgetReplyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Uri>) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris.addAll(uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfessionalBudgetBadgeIcon(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text("Enviar Orçamentos")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Esta mensagem e anexos serão visíveis apenas para o Síndico e o Conselho Fiscal.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Text("Anexe os documentos dos orçamentos.", style = MaterialTheme.typography.bodySmall)
                
                OutlinedButton(
                    onClick = { fileLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Anexar Orçamentos")
                }
                
                if (selectedUris.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().heightIn(max = 120.dp).verticalScroll(rememberScrollState())) {
                        selectedUris.forEach { uri ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(getFileName(context, uri), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Mensagem/Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(note, selectedUris.toList())
            }) {
                Text("Enviar ao Conselho")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetVotingCard(
    text: String,
    author: String,
    date: String,
    isFromMe: Boolean,
    currentUser: UserEntity,
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, Boolean) -> Unit,
    onShowCloseVotingDialog: () -> Unit,
    onOpenImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val totalVotes = attachments.sumOf { it.votes.size }
    val canVote = (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN") && !isVotingClosed

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth(0.92f)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        Column {
            // 1. Cabeçalho claro
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ProfessionalBudgetBadgeIcon(size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Votação de Orçamentos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Enviado por ${author.uppercase()} • $date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isConfidential || isSindicoOnly) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Confidencial",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            if (isFromMe) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isRead) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                    if (text.isNotEmpty() && text != "Mensagem sigilosa") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 2. Listagem dos itens de orçamentos e votos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (text == "Mensagem sigilosa") {
                    Text(
                        text = "Mensagem sigilosa",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray
                    )
                } else if (attachments.isEmpty()) {
                    Text(
                        text = "Nenhum orçamento anexado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    attachments.forEach { attWithVotes ->
                        val att = attWithVotes.attachment
                        val votesCount = attWithVotes.votes.size
                        val voteFraction = if (totalVotes > 0) votesCount.toFloat() / totalVotes else 0f
                        val votePercent = (voteFraction * 100).toInt()
                        val hasVoted = attWithVotes.votes.any { it.username == currentUser.username }
                        val isImg = isImageFile(att.fileName)

                        Surface(
                            color = if (hasVoted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (hasVoted) 1.5.dp else 0.5.dp,
                                color = if (hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Thumbnail/Icon, Filename, Download, Vote Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(0.5.dp, Color.LightGray),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable {
                                                if (isImg) onOpenImage(att.filePath)
                                                else openFile(context, att.filePath)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isImg) {
                                                val imageModel: Any = when {
                                                    att.filePath.startsWith("data:") -> {
                                                        try {
                                                            val pure = if (att.filePath.contains(",")) att.filePath.substringAfter(",") else att.filePath
                                                            android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                                                        } catch (_: Exception) { att.filePath }
                                                    }
                                                    att.filePath.startsWith("http") -> att.filePath
                                                    else -> File(att.filePath)
                                                }
                                                AsyncImage(
                                                    model = imageModel,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = att.fileName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Toque no arquivo para visualizar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    val canDownload = currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN"
                                    if (canDownload) {
                                        IconButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        downloadFile(context, att.filePath, att.fileName)
                                                    } else false
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, if (success) "Download concluído!" else "Falha no download", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                        }
                                    }

                                    // 3. Interação moderna e explícita (Botão circular de votação)
                                    if (!isVotingClosed && canVote) {
                                        Spacer(Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (hasVoted) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            border = BorderStroke(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    onToggleVote(att.id, hasVoted)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (hasVoted) Icons.Default.Check else Icons.Default.HowToVote,
                                                    contentDescription = if (hasVoted) "Voto confirmado" else "Votar neste orçamento",
                                                    tint = if (hasVoted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Barra de progresso visual e quantidade exata de votos
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$votesCount voto(s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$votePercent%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { voteFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(Modifier.height(6.dp))

                                // Nome dos conselheiros/síndicos que já votaram em cada item
                                if (attWithVotes.votes.isNotEmpty()) {
                                    val votersList = attWithVotes.votes.joinToString(", ") { it.username }
                                    Text(
                                        text = "Votado por: $votersList",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Nenhum voto registrado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // 4. Fechamento de votação mais inteligente
                if (isVotingClosed) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚠️ Esta votação foi encerrada",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    val canCloseVoting = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"
                    if (canCloseVoting) {
                        Button(
                            onClick = onShowCloseVotingDialog,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Encerrar Votação", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    text: String,
    author: String,
    date: String,
    isFromMe: Boolean,
    currentUser: UserEntity,
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    isBudget: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, Boolean) -> Unit,
    onCloseVoting: (String?, Double?, Int?, Double?, String?, String?) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var showCloseVotingDialog by remember { mutableStateOf(false) }

    val alignment = if (isFromMe) Alignment.End else Alignment.Start

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        if (isBudget) {
            BudgetVotingCard(
                text = text,
                author = author,
                date = date,
                isFromMe = isFromMe,
                currentUser = currentUser,
                isConfidential = isConfidential,
                isSindicoOnly = isSindicoOnly,
                isRead = isRead,
                isVotingClosed = isVotingClosed,
                attachments = attachments,
                isSelected = isSelected,
                onLongClick = onLongClick,
                onToggleVote = onToggleVote,
                onShowCloseVotingDialog = { showCloseVotingDialog = true },
                onOpenImage = { fullScreenImagePath = it }
            )
        } else {
            val bubbleColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isFromMe) Color(0xFFDCF8C6) else Color.White
            val shape = if (isFromMe) {
                RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
            } else {
                RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
            }

            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 1.dp,
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            author.uppercase(), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = if (isFromMe) Color(0xFF075E54) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isConfidential || isSindicoOnly) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = "Confidencial", 
                                Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    if (text.isNotEmpty()) {
                        Text(
                            text = text, 
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = if (text == "Mensagem sigilosa") FontStyle.Italic else FontStyle.Normal,
                            color = if (text == "Mensagem sigilosa") Color.Gray else Color.Unspecified
                        )
                    }
                    
                    if (attachments.isNotEmpty() && text != "Mensagem sigilosa") {
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            attachments.forEach { attWithVotes ->
                                val att = attWithVotes.attachment
                                val isImg = isImageFile(att.fileName)

                                Surface(
                                    color = if (isFromMe) Color(0xFFC3E8B0) else Color(0xFFF0F0F0),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        if (isImg) fullScreenImagePath = att.filePath
                                        else openFile(context, att.filePath)
                                    }
                                ) {
                                    Column {
                                        if (isImg) {
                                            val imageModel: Any = when {
                                                att.filePath.startsWith("data:") -> {
                                                    try {
                                                        val pure = if (att.filePath.contains(",")) att.filePath.substringAfter(",") else att.filePath
                                                        android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                                                    } catch (_: Exception) { att.filePath }
                                                }
                                                att.filePath.startsWith("http") -> att.filePath
                                                else -> File(att.filePath)
                                            }

                                            AsyncImage(
                                                model = imageModel,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .heightIn(max = 200.dp)
                                                    .background(Color.Black.copy(alpha = 0.05f)),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        }
                                        Row(
                                            Modifier.padding(8.dp).fillMaxWidth(0.7f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isImg) Icons.Default.Image else Icons.AutoMirrored.Filled.InsertDriveFile,
                                                null,
                                                Modifier.size(20.dp),
                                                tint = if (isFromMe) Color(0xFF075E54) else Color.Gray
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                att.fileName,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            val canDownload = currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN"
                                            if (canDownload) {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch(Dispatchers.IO) {
                                                            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                downloadFile(context, att.filePath, att.fileName)
                                                            } else {
                                                                false
                                                            }
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, if (success) "Download concluído!" else "Falha no download", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Download, null, Modifier.size(16.dp), tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.End)) {
                        Text(
                            date, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray, 
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (isFromMe) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isRead) Color(0xFF34B7F1) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCloseVotingDialog) {
        CloseVotingDialog(
            attachments = attachments,
            onDismiss = { showCloseVotingDialog = false },
            onConfirm = { name, total, count, instValue, start, dur ->
                onCloseVoting(name, total, count, instValue, start, dur)
                showCloseVotingDialog = false
            }
        )
    }

    if (fullScreenImagePath != null) {
        FullScreenImageDialog(
            filePath = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseVotingDialog(
    attachments: List<AttachmentWithVotes>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Double, String, String) -> Unit
) {
    var budgetName by remember { 
        mutableStateOf(attachments.maxByOrNull { it.votes.size }?.attachment?.fileName ?: "") 
    }
    var totalValueStr by remember { mutableStateOf("") }
    var installmentsCountStr by remember { mutableStateOf("1") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var duration by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val totalValue = (totalValueStr.toDoubleOrNull() ?: 0.0) / 100
    val installmentsCount = installmentsCountStr.toIntOrNull() ?: 1
    val installmentValue = if (installmentsCount > 0) totalValue / installmentsCount else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Definir Orçamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = budgetName,
                    onValueChange = { budgetName = it },
                    label = { Text("Nome do Orçamento/Fornecedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalValueStr,
                    onValueChange = { if (it.length <= 12) totalValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Total do Serviço") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = installmentsCountStr,
                    onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantidade de Parcelas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formatCurrency(installmentValue),
                    onValueChange = {},
                    label = { Text("Valor da Parcela (Calculado)") },
                    readOnly = true,
                    prefix = { Text("R$ ") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Data de Início") },
                    readOnly = true,
                    trailingIcon = { IconButton({ showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Tempo de Execução (ex: 5 dias)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(budgetName, totalValue, installmentsCount, installmentValue, startDate, duration)
                },
                enabled = totalValue > 0 && budgetName.isNotBlank()
            ) {
                Text("Confirmar e Encerrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { 
                            timeZone = TimeZone.getTimeZone("UTC") 
                        }.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FullScreenImageDialog(filePath: String, onDismiss: () -> Unit) {
    val imageModel: Any = when {
        filePath.startsWith("data:") -> {
            try {
                val pure = if (filePath.contains(",")) filePath.substringAfter(",") else filePath
                android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
            } catch (_: Exception) { filePath }
        }
        filePath.startsWith("http") -> filePath
        else -> File(filePath)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Imagem em tela cheia",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

fun isImageFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun downloadFile(context: Context, filePath: String, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val extension = fileName.substringAfterLast('.', "")
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/CondSuites")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                val url = URL(filePath)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.connect()

                resolver.openOutputStream(uri)?.use { output ->
                    conn.inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                val file = File(filePath)
                if (!file.exists()) return@withContext false
                resolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    try {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "Documento"
}

fun copyFileToInternalStorage(context: Context, uri: Uri): String? {
    val fileName = getFileName(context, uri)
    val storageDir = File(context.filesDir, "attachments")
    if (!storageDir.exists()) storageDir.mkdirs()
    
    val targetFile = File(storageDir, "${System.currentTimeMillis()}_$fileName")
    try {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        return targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun sha1(input: String): String {
    val bytes = java.security.MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun extractJsonValue(json: String, key: String): String? {
    val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val match = pattern.find(json)
    return match?.groupValues?.get(1)?.replace("\\/", "/")
}

fun uploadFileToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val cloudName = "dcondsuites"
            val uploadPreset = "condsuites"

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                onComplete(copyFileToInternalStorage(context, uri))
                return@launch
            }

            val fileName = getFileName(context, uri)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val boundary = "----CloudinaryBoundary" + System.currentTimeMillis()
            val url = URL("https://api.cloudinary.com/v1_1/$cloudName/auto/upload")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connectTimeout = 30000
                readTimeout = 30000
            }

            val dos = java.io.DataOutputStream(conn.outputStream)

            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            dos.writeBytes("$uploadPreset\r\n")

            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
            dos.write(bytes)
            dos.writeBytes("\r\n")

            dos.writeBytes("--$boundary--\r\n")
            dos.flush()
            dos.close()

            val responseCode = conn.responseCode
            if (responseCode == 200 || responseCode == 201) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val secureUrl = extractJsonValue(responseText, "secure_url") ?: extractJsonValue(responseText, "url")
                if (!secureUrl.isNullOrBlank()) {
                    android.util.Log.d("CLOUDINARY_UPLOAD", "Uploaded successfully to Cloudinary: $secureUrl")
                    onComplete(secureUrl)
                    return@launch
                }
            } else {
                val errText = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { "" }
                android.util.Log.e("CLOUDINARY_UPLOAD", "Cloudinary upload HTTP $responseCode: $errText")
            }

            val base64 = uriToBase64(context, uri)
            onComplete(base64 ?: copyFileToInternalStorage(context, uri))
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("CLOUDINARY_UPLOAD", "Error in Cloudinary upload: ${e.message}", e)
            val base64 = uriToBase64(context, uri)
            onComplete(base64 ?: copyFileToInternalStorage(context, uri))
        }
    }
}

fun uploadImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToCloudinary(context, uri, onComplete)
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmap == null) return null

        val maxWidth = 800
        val maxHeight = 800
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()
        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = maxWidth
            finalHeight = (maxWidth / ratio).toInt()
        } else {
            finalHeight = maxHeight
            finalWidth = (maxHeight * ratio).toInt()
        }
        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        
        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
        val bytes = outputStream.toByteArray()
        val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun openFile(context: Context, filePath: String) {
    if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível abrir o link do arquivo", Toast.LENGTH_SHORT).show()
        }
        return
    }

    val file = File(filePath)
    if (!file.exists()) {
        Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val type = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Não foi possível abrir o arquivo", Toast.LENGTH_SHORT).show()
    }
}

fun downloadContractFile(context: Context, filePath: String, fileName: String) {
    if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CoroutineScope(Dispatchers.Main).launch {
                val success = downloadFile(context, filePath, fileName)
                if (success) {
                    Toast.makeText(context, "Documento baixado com sucesso para Downloads/CondSuites!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Erro ao baixar documento", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
        return
    }

    val file = File(filePath)
    if (!file.exists()) {
        Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        CoroutineScope(Dispatchers.Main).launch {
            val success = downloadFile(context, filePath, fileName)
            if (success) {
                Toast.makeText(context, "Documento baixado com sucesso para Downloads/CondSuites!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erro ao baixar documento", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, fileName)
            file.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "Documento baixado na pasta Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível baixar o arquivo", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOccurrenceDialog(currentUser: UserEntity, dao: AppDao, onDismiss: () -> Unit, onConfirm: (String, String, String, Boolean, List<Uri>, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var apt by remember { mutableStateOf("") }
    var isCondo by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(false) }
    
    // Estados adicionais para modo Condomínio + Elevador
    var floor by remember { mutableStateOf("T") }
    var valueStr by remember { mutableStateOf("") }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentsCountStr by remember { mutableStateOf("1") }

    val defaultDest = when (currentUser.role) {
        "Conselheiro Fiscal" -> "CONSELHO"
        else -> "GERAL"
    }
    var destination by remember { mutableStateOf(defaultDest) } 
    
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris.addAll(uris)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Nova Ocorrência", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(thickness = 0.5.dp)

                if (currentUser.role != "Porteiro" && currentUser.role != "Zelador") {
                    Column {
                        Text("Encaminhar para:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = destination == "GERAL",
                                onClick = { destination = "GERAL" },
                                label = { Text("Zelador", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (destination == "GERAL") { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            val destLabel = if (currentUser.role == "Conselheiro Fiscal") "Síndico" else "Conselho"
                            FilterChip(
                                selected = destination == "CONSELHO",
                                onClick = { destination = "CONSELHO" },
                                label = { Text(destLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (destination == "CONSELHO") { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column {
                    Text("Local da Ocorrência:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = !isCondo,
                            onClick = { isCondo = false },
                            label = { Text("Unidade", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (!isCondo) { { Icon(Icons.Default.Apartment, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isCondo,
                            onClick = { isCondo = true },
                            label = { Text("Condomínio", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (isCondo) { { Icon(Icons.Default.Groups, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (!isCondo) {
                    OutlinedTextField(
                        value = apt, 
                        onValueChange = { apt = it }, 
                        label = { Text("Nº do Apartamento") }, 
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Apartment, null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                val subjectsList = remember { mutableStateListOf("Elevador Social", "Elevador de Serviço", "Piscina", "Jardim", "Vazamento", "Caixa D'água", "Câmeras", "Barulho", "Infiltração", "Limpeza") }
                var showAddSubjectDialog by remember { mutableStateOf(false) }
                var newSubjectText by remember { mutableStateOf("") }
                var expandedTitle by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTitle,
                        onExpandedChange = { expandedTitle = !expandedTitle },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Assunto / Título") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTitle) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTitle,
                            onDismissRequest = { expandedTitle = false }
                        ) {
                            subjectsList.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        title = selectionOption
                                        expandedTitle = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { showAddSubjectDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar Assunto", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                val isElevatorTitle = title == "Elevador Social" || title == "Elevador de Serviço" || title == "Elevador"

                if (isCondo && isElevatorTitle) {
                    Spacer(Modifier.height(8.dp))
                    
                    // Descrição Dropdown (Elevador)
                    var expandedDescDropdown by remember { mutableStateOf(false) }
                    val defaultDescs = listOf("TROCA DE DICTADOR", "TROCA DE BOTÃO", "TROCA DE VENTILADOR")
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedDescDropdown,
                        onExpandedChange = { expandedDescDropdown = !expandedDescDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("Descrição") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDescDropdown) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Build, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDescDropdown,
                            onDismissRequest = { expandedDescDropdown = false }
                        ) {
                            defaultDescs.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        desc = selectionOption
                                        expandedDescDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Andar Dropdown
                    var expandedFloor by remember { mutableStateOf(false) }
                    val floors = listOf("12", "11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "T", "1S", "2S", "3S", "4S")
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedFloor,
                        onExpandedChange = { expandedFloor = !expandedFloor },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = floor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Andar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloor) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Elevator, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFloor,
                            onDismissRequest = { expandedFloor = false }
                        ) {
                            floors.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        floor = selectionOption
                                        expandedFloor = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Valor (Opcional - Inibido para Zelador)
                    if (currentUser.role != "Zelador") {
                        OutlinedTextField(
                            value = valueStr,
                            onValueChange = { if (it.length <= 12) valueStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Valor (Opcional)") },
                            prefix = { Text("R$ ") },
                            visualTransformation = CurrencyVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        // Parcelado
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { isInstallment = !isInstallment }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                                    Text("Parcelado", fontWeight = FontWeight.Bold)
                                }
                                
                                if (isInstallment) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = installmentsCountStr,
                                        onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                                        label = { Text("Quantidade de Parcelas") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val count = installmentsCountStr.toIntOrNull() ?: 1
                                    val instValue = if (count > 0) totalVal / count else 0.0
                                    
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = formatCurrency(instValue),
                                        onValueChange = {},
                                        label = { Text("Valor da Parcela") },
                                        readOnly = true,
                                        prefix = { Text("R$ ") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc, 
                        onValueChange = { desc = it }, 
                        label = { Text("Descrição detalhada") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (showAddSubjectDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddSubjectDialog = false; newSubjectText = "" },
                        title = { Text("Adicionar Novo Assunto") },
                        text = {
                            OutlinedTextField(
                                value = newSubjectText,
                                onValueChange = { newSubjectText = it },
                                label = { Text("Nome do Assunto") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newSubjectText.isNotBlank()) {
                                        val trimmed = newSubjectText.trim()
                                        if (!subjectsList.contains(trimmed)) {
                                            subjectsList.add(trimmed)
                                        }
                                        title = trimmed
                                        newSubjectText = ""
                                        showAddSubjectDialog = false
                                    }
                                }
                            ) {
                                Text("Adicionar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddSubjectDialog = false; newSubjectText = "" }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                Surface(
                    color = if (isUrgent) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { isUrgent = !isUrgent }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Column {
                            Text("Urgente", fontWeight = FontWeight.Bold, color = if (isUrgent) Color.Red else Color.Unspecified)
                            Text("Priorizar esta ocorrência", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Column {
                    Text("Anexos:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Documentos/Fotos")
                    }
                    
                    if (selectedUris.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedUris.forEach { uri ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, Modifier.size(20.dp), tint = Color.Gray)
                                        Spacer(Modifier.width(8.dp))
                                        Text(getFileName(context, uri), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { 
                        Text("Cancelar", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) 
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            if (title.isNotBlank() && desc.isNotBlank() && (isCondo || apt.isNotBlank())) {
                                val finalApt = if (isCondo) "CONDOMÍNIO" else apt
                                
                                val finalDesc = if (isCondo && isElevatorTitle) {
                                    val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val payMethod = if (isInstallment) {
                                        val count = (installmentsCountStr.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                                        "Parcelado ($count x R$ ${formatCurrency(totalVal / count)})"
                                    } else "À vista"
                                    
                                    StringBuilder().apply {
                                        append("📅 DATA: $currentDate\n")
                                        append("📝 DESCRIÇÃO: $desc\n")
                                        append("🏢 ANDAR: $floor")
                                        if (currentUser.role != "Zelador" && valueStr.isNotBlank() && totalVal > 0) {
                                            append("\n💰 VALOR TOTAL: R$ ${formatCurrency(totalVal)}\n")
                                            append("💳 PAGAMENTO: $payMethod")
                                        }
                                    }.toString()
                                } else desc

                                onConfirm(title, finalDesc, finalApt, isUrgent, selectedUris.toList(), destination)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank() && desc.isNotBlank() && (isCondo || apt.isNotBlank()),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Registrar", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun OccurrenceLogsDialog(dao: AppDao, onDismiss: () -> Unit) {
    val logs by dao.getAllOccurrenceLogs().collectAsState(initial = emptyList())
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Logs de Auditoria", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(logs) { log ->
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${log.username} ${log.action} a ocorrência #${log.occurrenceId}", fontWeight = FontWeight.Bold)
                            Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            HorizontalDivider(Modifier.padding(top = 4.dp), thickness = 0.5.dp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}
@Composable
fun LoginScreen(dao: AppDao, onLoginSuccess: (UserEntity) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberUser by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val rememberedUsers by UserPreferences.getRememberedUsernames(context).collectAsState(initial = emptySet())

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Filled.Apartment, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("CondSuites", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Administração de Condomínio", style = MaterialTheme.typography.titleMedium)
                
                if (rememberedUsers.isNotEmpty()) {
                    Text("Usuários recentes:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(rememberedUsers.toList()) { user ->
                            InputChip(
                                selected = username == user,
                                onClick = { username = user },
                                label = { Text(user) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remover",
                                        modifier = Modifier.size(16.dp).clickable {
                                            scope.launch { UserPreferences.removeUsername(context, user) }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Usuário") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = rememberUser, onCheckedChange = { rememberUser = it })
                    Text("Lembrar usuário", style = MaterialTheme.typography.bodySmall)
                }
                
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val user = withContext(Dispatchers.IO) {
                                    dao.getUserByUsername(username)
                                }
                                if (user != null && user.password == password) {
                                    if (rememberUser) {
                                        try {
                                            UserPreferences.saveUsername(context, username)
                                        } catch (_: Exception) {}
                                    }
                                    onLoginSuccess(user)
                                } else {
                                    errorMessage = "Usuário ou senha incorretos"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erro ao fazer login: ${e.localizedMessage}"
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENTRAR")
                }
            }
        }
    }
}

@Composable
fun UserManagementScreen(dao: AppDao, scope: CoroutineScope) {
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Gestão de Usuários", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Perfil: ${user.role}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { userToEdit = user }) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            if (user.username != "admin") {
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deleteUser(user.id)
                                        FirestoreSyncManager.syncUser(user, isDelete = true)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        RegisterUserDialog(onDismiss = { showAddDialog = false }) { newUser ->
            scope.launch(Dispatchers.IO) {
                val existing = dao.getUserByUsername(newUser.username)
                val userToSave = if (existing != null) newUser.copy(id = existing.id) else newUser
                dao.insertUser(userToSave)
                FirestoreSyncManager.syncUser(userToSave)
            }
            showAddDialog = false
        }
    }

    if (userToEdit != null) {
        EditUserDialog(user = userToEdit!!, onDismiss = { userToEdit = null }) { updatedUser ->
            scope.launch(Dispatchers.IO) {
                dao.insertUser(updatedUser)
                FirestoreSyncManager.syncUser(updatedUser)
            }
            userToEdit = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (UserEntity) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var password by remember { mutableStateOf(user.password) }
    var role by remember { mutableStateOf(user.role) }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Conselheiro Fiscal", "Zelador", "Síndico", "Porteiro", "ADMIN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuário: ${user.username}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nome de Usuário") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nível de Acesso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(user.copy(username = username.trim(), password = password.trim(), role = role))
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ExportNotificationsScreen(dao: AppDao, scope: CoroutineScope) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val success = ExcelHelper.exportNotificationsTable(context, dao, it)
                exportResult = if (success) "Tabela de notificações exportada com sucesso!" else "Falha ao exportar tabela."
                isExporting = false
            }
        }
    }

    val exportProgressLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val success = ExcelHelper.exportAllProgressTable(context, dao, it)
                exportResult = if (success) "Histórico de andamentos exportado com sucesso!" else "Falha ao exportar andamentos."
                isExporting = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val result = ExcelHelper.importNotificationsTable(context, dao, it)
                exportResult = result
                isExporting = false
            }
        }
    }

    val importProgressLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val result = ExcelHelper.importAllProgressTable(context, dao, it)
                exportResult = result
                isExporting = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Description, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text("Exportar / Importar Dados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Gerencie os registros e históricos do sistema via Excel.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text("Registros de Unidades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { 
                val fileName = "Notificacoes_CondSuites_${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}.xlsx"
                exportLauncher.launch(fileName) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isExporting,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isExporting) {
                LinearProgressIndicator(modifier = Modifier.width(100.dp))
            } else {
                Icon(Icons.Default.Upload, null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Unidades")
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { 
                importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isExporting,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Importar Unidades")
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(thickness = 0.5.dp)
        Spacer(Modifier.height(32.dp))

        Text("Histórico de Andamentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { 
                val fileName = "Andamentos_CondSuites_${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}.xlsx"
                exportProgressLauncher.launch(fileName) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isExporting,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.History, null)
            Spacer(Modifier.width(8.dp))
            Text("Exportar Andamentos")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { 
                importProgressLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isExporting,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Importar Andamentos")
        }
        
        Spacer(Modifier.height(40.dp))
    }

    if (exportResult != null) {
        AlertDialog(
            onDismissRequest = { exportResult = null },
            title = { Text("Resultado do Processamento") },
            text = { 
                Box(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(exportResult!!) 
                }
            },
            confirmButton = { Button({ exportResult = null }) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSwitchUserDialog(
    dao: AppDao,
    onDismiss: () -> Unit,
    onUserSelected: (UserEntity) -> Unit
) {
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trocar Usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione um usuário para entrar sem fechar o app.", style = MaterialTheme.typography.bodySmall)
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users) { user ->
                        FilterChip(
                            selected = selectedUser == user,
                            onClick = { selectedUser = user; error = null },
                            label = { Text(user.username) },
                            leadingIcon = if (selectedUser == user) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null
                        )
                    }
                }
                
                if (selectedUser != null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Senha para ${selectedUser!!.username}") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                if (error != null) {
                    Text(error!!, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUser != null && selectedUser!!.password == password) {
                        onUserSelected(selectedUser!!)
                    } else {
                        error = "Senha incorreta"
                    }
                },
                enabled = selectedUser != null && password.isNotBlank()
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserDialog(onDismiss: () -> Unit, onConfirm: (UserEntity) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Porteiro") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Conselheiro Fiscal", "Zelador", "Síndico", "Porteiro", "ADMIN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(username, { username = it }, label = { Text("Nome de Usuário") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(password, { password = it }, label = { Text("Senha") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nível de Acesso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(UserEntity(username = username, password = password, role = role)) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun RecursiveNavItem(item: NavigationItem, currentScreen: Screen, expandedItems: Set<String>, level: Int = 0, onExpandToggle: (String) -> Unit, onScreenSelect: (Screen) -> Unit) {
    val isExpanded = expandedItems.contains(item.screen.route)
    val hasSubItems = item.subItems.isNotEmpty()
    val isSelected = currentScreen.route == item.screen.route
    Column {
        NavigationDrawerItem(
            label = { Text(item.screen.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            selected = isSelected && !hasSubItems,
            onClick = { if (hasSubItems) onExpandToggle(item.screen.route) else onScreenSelect(item.screen) },
            icon = { Icon(item.screen.icon, null) },
            badge = { if (hasSubItems) Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(start = (level * 12).dp).padding(horizontal = 4.dp, vertical = 2.dp)
        )
        if (hasSubItems && isExpanded) {
            item.subItems.forEach { subItem -> RecursiveNavItem(subItem, currentScreen, expandedItems, level + 1, onExpandToggle, onScreenSelect) }
        }
    }
}

// --- RELATÓRIOS PROFISSIONAIS DE ACORDOS ---

fun generateAgreementsHtmlReport(
    agreements: List<AgreementWithInstallments>,
    statusFilterText: String,
    periodText: String,
    valueRangeText: String
): String {
    val totalCount = agreements.size
    val totalAgreedDebt = agreements.sumOf { it.agreement.totalDebt }
    val totalPaidValue = agreements.sumOf { item ->
        item.installments.filter { it.isPaid }.sumOf { it.value }
    }
    val totalRemainingValue = agreements.sumOf { item ->
        item.installments.filter { !it.isPaid }.sumOf { it.value }
    }
    val clearanceRate = if (totalAgreedDebt > 0) (totalPaidValue * 100 / totalAgreedDebt).toInt() else 0
    val lawsuitCount = agreements.count { it.agreement.isLawsuit }
    val overdueCount = agreements.count { item ->
        item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }
    }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Gerencial de Acordos</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #2E7D32; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #2E7D32; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E8F5E9; border: 1px solid #C8E6C9; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1B5E20; font-weight: bold; }
                .kpi-value { font-size: 15px; font-weight: bold; color: #2E7D32; margin-top: 4px; }
                .section-title { font-size: 15px; color: #1B5E20; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #2E7D32; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-quitado { background: #2E7D32; }
                .badge-emdia { background: #0288D1; }
                .badge-ematraso { background: #E65100; }
                .badge-ajuizado { background: #C62828; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Gerencial de Acordos & Diagnóstico Financeiro</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText &nbsp;|&nbsp;
                <strong>Faixa de Valor:</strong> $valueRangeText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Acordos</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Valor Acordado</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalAgreedDebt)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Arrecadado</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalPaidValue)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Saldo Pendente</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalRemainingValue)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa Quitação</div>
                    <div class="kpi-value">$clearanceRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Diagnóstico Financeiro</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                Este relatório analítico apresenta o desempenho da recuperação de crédito do condomínio.
                Atualmente, do total negociado de <strong>R$ ${formatCurrency(totalAgreedDebt)}</strong>, já foram arrecadados <strong>R$ ${formatCurrency(totalPaidValue)}</strong> (taxa de quitação de <strong>$clearanceRate%</strong>), restando um saldo devedor de <strong>R$ ${formatCurrency(totalRemainingValue)}</strong>.
                ${if (overdueCount > 0) "Observam-se <strong>$overdueCount acordo(s) com parcelas em atraso</strong> que necessitam de acompanhamento intensivo ou renegociação para evitar inadimplência persistente." else "Todos os acordos vigentes estão adimplentes e sem atraso registrado nas parcelas."}
                ${if (lawsuitCount > 0) " Adicionalmente, <strong>$lawsuitCount acordo(s)</strong> possuem cobrança judicial/ajuizada associada." else ""}
                <br><strong>Recomendação da Gestão:</strong> Manter a régua de cobrança ativa com envio de lembretes antes dos vencimentos e priorizar a execução judicial ou notificação extrajudicial para acordos que excedam 30 dias de atraso.
            </p>

            <div class="section-title">📋 Detalhamento dos Acordos</div>
    """.trimIndent())

    if (agreements.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum acordo encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 10%;">Apto</th>
                        <th style="width: 22%;">Proprietário</th>
                        <th style="width: 12%;">Data Acordo</th>
                        <th style="width: 16%;">Dívida Total</th>
                        <th style="width: 16%;">Valor Pago / Restante</th>
                        <th style="width: 10%;">Parcelas</th>
                        <th style="width: 10%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
            val agg = item.agreement
            val paidCount = item.installments.count { it.isPaid }
            val paidSum = item.installments.filter { it.isPaid }.sumOf { it.value }
            val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
            val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
            val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

            val statusText: String
            val badgeClass: String
            if (isAllPaid) {
                statusText = "QUITADO"
                badgeClass = "badge-quitado"
            } else if (agg.isLawsuit) {
                statusText = "AJUIZADO"
                badgeClass = "badge-ajuizado"
            } else if (hasOverdue) {
                statusText = "EM ATRASO"
                badgeClass = "badge-ematraso"
            } else {
                statusText = "EM DIA"
                badgeClass = "badge-emdia"
            }

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>Apto ${agg.apartment}</strong></td>
                    <td>${agg.ownerName.uppercase()}</td>
                    <td>${agg.date}</td>
                    <td>R$ ${formatCurrency(agg.totalDebt)}</td>
                    <td><small style='color:#2E7D32'>R$ ${formatCurrency(paidSum)}</small><br><small style='color:#C62828'>Rest: R$ ${formatCurrency(remSum)}</small></td>
                    <td>$paidCount/${agg.installmentsCount}</td>
                    <td><span class="badge $badgeClass">$statusText</span></td>
                </tr>
            """.trimIndent())
        }

        html.append("</tbody></table>")
    }

    html.append("""
        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Responsável Financeiro</strong><br>
                Departamento de Cobrança / Acordos
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateAgreementsTextReport(
    agreements: List<AgreementWithInstallments>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = agreements.size
    val totalAgreed = agreements.sumOf { it.agreement.totalDebt }
    val totalPaid = agreements.sumOf { item -> item.installments.filter { it.isPaid }.sumOf { it.value } }
    val totalRemaining = agreements.sumOf { item -> item.installments.filter { !it.isPaid }.sumOf { it.value } }
    val clearanceRate = if (totalAgreed > 0) (totalPaid * 100 / totalAgreed).toInt() else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE ACORDOS\n")
    sb.append("📋 GESTÃO CONDOMINIAL & RECUPERAÇÃO DE CRÉDITO\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO FINANCEIRO / POSIÇÃO DE CRÉDITO:\n")
    sb.append(" • Total de Acordos Registrados: $totalCount\n")
    sb.append(" • Valor Total Negociado: R$ ${formatCurrency(totalAgreed)}\n")
    sb.append(" • Valor Arrecadado / Pago: R$ ${formatCurrency(totalPaid)}\n")
    sb.append(" • Saldo Pendente: R$ ${formatCurrency(totalRemaining)}\n")
    sb.append(" • Taxa de Quitação: $clearanceRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (agreements.isEmpty()) {
        sb.append("Nenhum acordo encontrado para os filtros selecionados.\n")
    } else {
        agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
            val agg = item.agreement
            val paidCount = item.installments.count { it.isPaid }
            val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
            sb.append("[${idx + 1}] Apto ${agg.apartment} - ${agg.ownerName.uppercase()}\n")
            sb.append("    Data: ${agg.date} | Total Dívida: R$ ${formatCurrency(agg.totalDebt)}\n")
            sb.append("    Parcelas: $paidCount/${agg.installmentsCount} pagas | Restante: R$ ${formatCurrency(remSum)}\n")
            if (agg.isLawsuit) {
                sb.append("    ⚖️ Status: AJUIZADO\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Financeiro: ___________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAgreementsScreen(dao: AppDao, context: Context) {
    val agreements by dao.getActiveAgreements().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }
    var selectedValueRangeFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val filteredList = remember(
        agreements, searchText, selectedStatusFilter, selectedPeriodFilter, selectedValueRangeFilter, selectedMonth, selectedYear
    ) {
        agreements.filter { item ->
            val agg = item.agreement
            val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
            val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

            val matchesStatus = when (selectedStatusFilter) {
                "Em Dia" -> !isAllPaid && !hasOverdue && !agg.isLawsuit
                "Em Atraso" -> hasOverdue && !isAllPaid
                "Quitados" -> isAllPaid
                "Ajuizados" -> agg.isLawsuit
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(agg.date) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val diffDays = (System.currentTimeMillis() - date.time) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val matchesValue = when (selectedValueRangeFilter) {
                "Até R$ 1.000" -> agg.totalDebt <= 1000.0
                "R$ 1.000 a R$ 5.000" -> agg.totalDebt in 1000.0..5000.0
                "R$ 5.000 a R$ 10.000" -> agg.totalDebt in 5000.0..10000.0
                "Acima de R$ 10.000" -> agg.totalDebt > 10000.0
                else -> true
            }

            val matchesSearch = searchText.isBlank() ||
                agg.apartment.contains(searchText, ignoreCase = true) ||
                agg.ownerName.contains(searchText, ignoreCase = true) ||
                agg.quotaMonths.contains(searchText, ignoreCase = true)

            matchesStatus && matchesPeriod && matchesValue && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalAgreed = filteredList.sumOf { it.agreement.totalDebt }
    val totalPaid = filteredList.sumOf { item -> item.installments.filter { it.isPaid }.sumOf { it.value } }
    val totalRemaining = filteredList.sumOf { item -> item.installments.filter { !it.isPaid }.sumOf { it.value } }
    val clearanceRate = if (totalAgreed > 0) (totalPaid * 100 / totalAgreed).toInt() else 0
    val lawsuitCount = filteredList.count { it.agreement.isLawsuit }

    val activeFilterCount = (if (selectedStatusFilter != "Todos") 1 else 0) +
            (if (selectedPeriodFilter != "Todos") 1 else 0) +
            (if (selectedValueRangeFilter != "Todos") 1 else 0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Painel de Indicadores de Acordos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgreementsMetricCard("Total Acordos", totalCount.toString(), Icons.Filled.Handshake, Modifier.weight(1f))
                        AgreementsMetricCard("Total Acordado", "R$ ${formatCurrency(totalAgreed)}", Icons.Default.AccountBalance, Modifier.weight(1.2f))
                        AgreementsMetricCard("Arrecadado", "R$ ${formatCurrency(totalPaid)}", Icons.Default.CheckCircle, Modifier.weight(1.2f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgreementsMetricCard("Saldo Pendente", "R$ ${formatCurrency(totalRemaining)}", Icons.Default.ReceiptLong, Modifier.weight(1f))
                        AgreementsMetricCard("Taxa Quitação", "$clearanceRate%", Icons.Default.Analytics, Modifier.weight(1f))
                        AgreementsMetricCard("Ajuizados ⚖️", lawsuitCount.toString(), Icons.Default.Gavel, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar apto ou nome...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAdvancedFiltersDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filtros ${if (activeFilterCount > 0) "($activeFilterCount)" else ""}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Em Dia", onClick = { selectedStatusFilter = "Em Dia" }, label = { Text("Em Dia", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Em Atraso", onClick = { selectedStatusFilter = "Em Atraso" }, label = { Text("Atraso", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Quitados", onClick = { selectedStatusFilter = "Quitados" }, label = { Text("Quitados", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Filtros ativos ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedStatusFilter = "Todos"
                            selectedPeriodFilter = "Todos"
                            selectedValueRangeFilter = "Todos"
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar Filtros", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir Relatório (PDF)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
                            val textReport = generateAgreementsTextReport(filteredList, selectedStatusFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Acordos"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Acordos Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum acordo encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList.sortedBy { naturalSortApartments(it.agreement.apartment) }) { item ->
                val isExpanded = expandedCardId == item.agreement.id

                AgreementReportCard(
                    item = item,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.agreement.id
                    },
                    onShare = {
                        val text = generateElegantAgreementReportText(item)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar Acordo"))
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        AgreementsAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedValueRangeFilter = selectedValueRangeFilter,
            onValueRangeChange = { selectedValueRangeFilter = it },
            selectedPeriodFilter = selectedPeriodFilter,
            onPeriodChange = { selectedPeriodFilter = it },
            selectedMonth = selectedMonth,
            onMonthChange = { selectedMonth = it },
            selectedYear = selectedYear,
            onYearChange = { selectedYear = it },
            monthLabels = monthLabels,
            onDismiss = { showAdvancedFiltersDialog = false }
        )
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateAgreementsHtmlReport(filteredList, selectedStatusFilter, periodText, selectedValueRangeFilter)
        val textReport = generateAgreementsTextReport(filteredList, selectedStatusFilter, periodText)

        AgreementsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Geral_Acordos")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Acordos"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun AgreementsMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun AgreementReportCard(
    item: AgreementWithInstallments,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val agg = item.agreement
    val paidCount = item.installments.count { it.isPaid }
    val paidSum = item.installments.filter { it.isPaid }.sumOf { it.value }
    val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
    val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
    val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

    val (statusText, statusColor) = when {
        isAllPaid -> "QUITADO" to Color(0xFF2E7D32)
        agg.isLawsuit -> "AJUIZADO ⚖️" to Color(0xFFC62828)
        hasOverdue -> "EM ATRASO 🚨" to Color(0xFFE65100)
        else -> "EM DIA" to Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Apto ${agg.apartment} • ${agg.ownerName.uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Dívida: R$ ${formatCurrency(agg.totalDebt)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Pago: R$ ${formatCurrency(paidSum)} | Resta: R$ ${formatCurrency(remSum)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Data do Acordo: ${agg.date}", style = MaterialTheme.typography.labelMedium)
                if (agg.quotaMonths.isNotBlank()) {
                    Text("Cotas Acordadas: ${agg.quotaMonths.replace(";", ", ")}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                Text("Cronograma de Parcelas ($paidCount/${agg.installmentsCount}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                item.installments.sortedBy { it.number }.forEach { inst ->
                    val instPaid = inst.isPaid
                    val instColor = if (instPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(Modifier.padding(8.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("${inst.number}/${agg.installmentsCount} • Venc: ${inst.dueDate}", style = MaterialTheme.typography.bodySmall)
                            Text("R$ ${formatCurrency(inst.value)} • ${if (instPaid) "PAGO" else "EM ABERTO"}", style = MaterialTheme.typography.bodySmall, color = instColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
    selectedValueRangeFilter: String,
    onValueRangeChange: (String) -> Unit,
    selectedPeriodFilter: String,
    onPeriodChange: (String) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    monthLabels: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Busca Avançada de Acordos") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status do Acordo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val statuses = listOf("Todos", "Em Dia", "Em Atraso", "Quitados", "Ajuizados")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        statuses.take(3).forEach { st ->
                            FilterChip(selected = selectedStatusFilter == st, onClick = { onStatusChange(st) }, label = { Text(st, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        statuses.drop(3).forEach { st ->
                            FilterChip(selected = selectedStatusFilter == st, onClick = { onStatusChange(st) }, label = { Text(st, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Column {
                    Text("Faixa de Valor da Dívida:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val ranges = listOf("Todos", "Até R$ 1.000", "R$ 1.000 a R$ 5.000", "R$ 5.000 a R$ 10.000", "Acima de R$ 10.000")
                    var expandedVal by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedVal, onExpandedChange = { expandedVal = !expandedVal }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedValueRangeFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione a faixa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVal) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedVal, onDismissRequest = { expandedVal = false }) {
                            ranges.forEach { r ->
                                DropdownMenuItem(text = { Text(r) }, onClick = { onValueRangeChange(r); expandedVal = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Período de Análise:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val periods = listOf("Todos", "Últimos 30 Dias", "Últimos 90 Dias", "Este Ano", "Mês/Ano Específico")
                    var expandedPeriod by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPeriodFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por período") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                            periods.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { onPeriodChange(p); expandedPeriod = false })
                            }
                        }
                    }

                    if (selectedPeriodFilter == "Mês/Ano Específico") {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expM by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expM = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expM, onDismissRequest = { expM = false }) {
                                    monthLabels.forEachIndexed { idx, m ->
                                        DropdownMenuItem(text = { Text(m) }, onClick = { onMonthChange(idx); expM = false })
                                    }
                                }
                            }

                            var expY by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expY = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ano: $selectedYear")
                                }
                                DropdownMenu(expanded = expY, onDismissRequest = { expY = false }) {
                                    (2024..2030).forEach { yr ->
                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = { onYearChange(yr); expY = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Aplicar Filtros")
            }
        }
    )
}

@Composable
fun AgreementsReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório Profissional") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount acordo(s). Próximo passo:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

// --- RELATÓRIOS PROFISSIONAIS DE PROCESSOS AJUIZADOS ---

fun generateLawsuitsHtmlReport(
    lawsuits: List<LawsuitWithProgress>,
    statusFilterText: String,
    forumFilterText: String,
    periodText: String
): String {
    val totalCount = lawsuits.size
    val totalDebt = lawsuits.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = lawsuits.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = lawsuits.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val ongoingCount = totalCount - finishedCount
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Jurídico de Processos Ajuizados</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #4A148C; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #4A148C; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #F3E5F5; border: 1px solid #E1BEE7; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #4A148C; font-weight: bold; }
                .kpi-value { font-size: 15px; font-weight: bold; color: #6A1B9A; margin-top: 4px; }
                .section-title { font-size: 15px; color: #4A148C; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #4A148C; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-andamento { background: #0288D1; }
                .badge-exito { background: #2E7D32; }
                .badge-acordo { background: #E65100; }
                .badge-suspenso { background: #757575; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Executivo e Parecer Jurídico de Processos Ajuizados</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Fórum:</strong> $forumFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Ações</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Dívida Ajuizada</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalDebt)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Recuperado / Êxito</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalSuccess)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Em Andamento</div>
                    <div class="kpi-value">$ongoingCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa de Êxito</div>
                    <div class="kpi-value">$successRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Parecer Jurídico</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                O contencioso judicial do condomínio é composto por <strong>$totalCount processo(s) ajuizado(s)</strong>, perfazendo um montante total em discussão de <strong>R$ ${formatCurrency(totalDebt)}</strong>.
                Até a presente data, obteve-se êxito/recuperação judicial no montante de <strong>R$ ${formatCurrency(totalSuccess)}</strong>, representando uma taxa de resolução favorável de <strong>$successRate%</strong> ($finishedCount processo(s) finalizado(s) e $ongoingCount em andamento).
                <br><strong>Parecer & Recomendações Jurídicas:</strong>
                Recomenda-se o acompanhamento quinzenal dos andamentos processuais junto às varas cíveis/fóruns correspondentes, solicitando penhora de bens/contas via Sisbajud/Renajud nos processos em fase de execução em que não houve pagamento voluntário. Para ações com proposta de conciliação, avaliar a homologação judicial do acordo para preservação do título executivo.
            </p>

            <div class="section-title">📋 Detalhamento dos Processos Ajuizados</div>
    """.trimIndent())

    if (lawsuits.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum processo ajuizado encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 10%;">Apto</th>
                        <th style="width: 20%;">Proprietário</th>
                        <th style="width: 18%;">Nº Processo / Fórum</th>
                        <th style="width: 14%;">Dívida Original</th>
                        <th style="width: 14%;">Valor Êxito</th>
                        <th style="width: 10%;">Data Ajuiz.</th>
                        <th style="width: 10%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        lawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }.forEachIndexed { idx, item ->
            val law = item.lawsuit
            val badgeClass = when {
                law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true) -> "badge-exito"
                law.status.contains("Acordo", ignoreCase = true) -> "badge-acordo"
                law.status.contains("Suspens", ignoreCase = true) -> "badge-suspenso"
                else -> "badge-andamento"
            }

            val successDisplay = if (law.successValue != null && law.successValue!! > 0) "R$ ${formatCurrency(law.successValue!!)}" else "-"

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>Apto ${law.apartment}</strong></td>
                    <td>${law.ownerName.uppercase()}</td>
                    <td><strong>${law.processNumber.ifBlank { "N/I" }}</strong><br><small style='color:#666'>${law.forum}</small></td>
                    <td>R$ ${formatCurrency(law.totalDebt)}</td>
                    <td><strong style='color:#2E7D32'>$successDisplay</strong></td>
                    <td>${law.registrationDate}</td>
                    <td><span class="badge $badgeClass">${law.status}</span></td>
                </tr>
            """.trimIndent())
        }

        html.append("</tbody></table>")
    }

    html.append("""
        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Assessoria Jurídica / Advogado</strong><br>
                OAB Responsável pelo Contencioso
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateLawsuitsTextReport(
    lawsuits: List<LawsuitWithProgress>,
    statusFilterText: String,
    forumFilterText: String,
    periodText: String
): String {
    val totalCount = lawsuits.size
    val totalDebt = lawsuits.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = lawsuits.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = lawsuits.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE PROCESSOS AJUIZADOS\n")
    sb.append("⚖️ CONTENCIOSO JUDICIAL & COBRANÇA JURÍDICA\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Fórum: $forumFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO JURÍDICO / INDICADORES:\n")
    sb.append(" • Total de Ações Ajuizadas: $totalCount\n")
    sb.append(" • Dívida Total Ajuizada: R$ ${formatCurrency(totalDebt)}\n")
    sb.append(" • Valor Recuperado / Êxito: R$ ${formatCurrency(totalSuccess)}\n")
    sb.append(" • Processos Concluídos com Êxito: $finishedCount\n")
    sb.append(" • Taxa de Êxito Judicial: $successRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (lawsuits.isEmpty()) {
        sb.append("Nenhum processo ajuizado encontrado para os filtros selecionados.\n")
    } else {
        lawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }.forEachIndexed { idx, item ->
            val law = item.lawsuit
            sb.append("[${idx + 1}] Apto ${law.apartment} - ${law.ownerName.uppercase()}\n")
            sb.append("    Nº Processo: ${law.processNumber.ifBlank { "Não informado" }}\n")
            sb.append("    Fórum: ${law.forum} | Data Ajuizamento: ${law.registrationDate}\n")
            sb.append("    Dívida: R$ ${formatCurrency(law.totalDebt)} | Status: ${law.status}\n")
            if (law.successValue != null && law.successValue!! > 0) {
                sb.append("    Valor Êxito: R$ ${formatCurrency(law.successValue!!)}\n")
            }
            if (item.progress.isNotEmpty()) {
                val lastProg = item.progress.last()
                sb.append("    Último Andamento [${lastProg.date}]: ${lastProg.description}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Advogado/OAB: _________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsLawsuitsScreen(dao: AppDao, context: Context) {
    val lawsuitsState by dao.getLawsuitsWithProgress().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedForumFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }
    var selectedValueRangeFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val uniqueForums = remember(lawsuitsState) {
        listOf("Todos") + lawsuitsState.map { it.lawsuit.forum }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredList = remember(
        lawsuitsState, searchText, selectedStatusFilter, selectedForumFilter,
        selectedPeriodFilter, selectedValueRangeFilter, selectedMonth, selectedYear
    ) {
        lawsuitsState.filter { item ->
            val law = item.lawsuit

            val matchesStatus = when (selectedStatusFilter) {
                "Em Andamento" -> !law.isFinished && !law.status.contains("Conclu", ignoreCase = true)
                "Concluído / Êxito" -> law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true)
                "Acordo Judicial" -> law.status.contains("Acordo", ignoreCase = true)
                "Suspenso" -> law.status.contains("Suspens", ignoreCase = true)
                else -> true
            }

            val matchesForum = selectedForumFilter == "Todos" || law.forum.equals(selectedForumFilter, ignoreCase = true)

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(law.registrationDate) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val diffDays = (System.currentTimeMillis() - date.time) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val matchesValue = when (selectedValueRangeFilter) {
                "Até R$ 5.000" -> law.totalDebt <= 5000.0
                "R$ 5.000 a R$ 15.000" -> law.totalDebt in 5000.0..15000.0
                "Acima de R$ 15.000" -> law.totalDebt > 15000.0
                else -> true
            }

            val fullText = "${law.apartment} ${law.ownerName} ${law.processNumber} ${law.forum} ${law.status} ${item.progress.joinToString(" ") { it.description }}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesForum && matchesPeriod && matchesValue && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalDebt = filteredList.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = filteredList.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = filteredList.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val ongoingCount = totalCount - finishedCount
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0

    val activeFilterCount = (if (selectedStatusFilter != "Todos") 1 else 0) +
            (if (selectedForumFilter != "Todos") 1 else 0) +
            (if (selectedPeriodFilter != "Todos") 1 else 0) +
            (if (selectedValueRangeFilter != "Todos") 1 else 0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Painel de Indicadores do Contencioso Jurídico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LawsuitsMetricCard("Total Ações", totalCount.toString(), Icons.Filled.Gavel, Modifier.weight(1f))
                        LawsuitsMetricCard("Dívida Ajuizada", "R$ ${formatCurrency(totalDebt)}", Icons.Default.AccountBalance, Modifier.weight(1.2f))
                        LawsuitsMetricCard("Recuperado / Êxito", "R$ ${formatCurrency(totalSuccess)}", Icons.Default.CheckCircle, Modifier.weight(1.2f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LawsuitsMetricCard("Em Andamento", ongoingCount.toString(), Icons.Default.History, Modifier.weight(1f))
                        LawsuitsMetricCard("Concluídos", finishedCount.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        LawsuitsMetricCard("Taxa Êxito", "$successRate%", Icons.Default.Analytics, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar processo, apto, fórum...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAdvancedFiltersDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filtros ${if (activeFilterCount > 0) "($activeFilterCount)" else ""}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Em Andamento", onClick = { selectedStatusFilter = "Em Andamento" }, label = { Text("Andamento", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Concluído / Êxito", onClick = { selectedStatusFilter = "Concluído / Êxito" }, label = { Text("Êxito", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Filtros ativos ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedStatusFilter = "Todos"
                            selectedForumFilter = "Todos"
                            selectedPeriodFilter = "Todos"
                            selectedValueRangeFilter = "Todos"
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar Filtros", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir Relatório (PDF)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
                            val textReport = generateLawsuitsTextReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Ajuizados"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Processos Ajuizados Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum processo ajuizado encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList.sortedBy { naturalSortApartments(it.lawsuit.apartment) }) { item ->
                val isExpanded = expandedCardId == item.lawsuit.id

                LawsuitReportCard(
                    item = item,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.lawsuit.id
                    },
                    onShare = {
                        shareLawsuitReport(context, item.lawsuit)
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        LawsuitsAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedForumFilter = selectedForumFilter,
            onForumChange = { selectedForumFilter = it },
            uniqueForums = uniqueForums,
            selectedValueRangeFilter = selectedValueRangeFilter,
            onValueRangeChange = { selectedValueRangeFilter = it },
            selectedPeriodFilter = selectedPeriodFilter,
            onPeriodChange = { selectedPeriodFilter = it },
            selectedMonth = selectedMonth,
            onMonthChange = { selectedMonth = it },
            selectedYear = selectedYear,
            onYearChange = { selectedYear = it },
            monthLabels = monthLabels,
            onDismiss = { showAdvancedFiltersDialog = false }
        )
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateLawsuitsHtmlReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)
        val textReport = generateLawsuitsTextReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)

        LawsuitsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Processos_Ajuizados")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Ajuizados"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun LawsuitsMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun LawsuitReportCard(
    item: LawsuitWithProgress,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val law = item.lawsuit
    val isFinished = law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true)

    val (statusText, statusColor) = when {
        isFinished -> "CONCLUÍDO / ÊXITO 🏆" to Color(0xFF2E7D32)
        law.status.contains("Acordo", ignoreCase = true) -> "ACORDO JUDICIAL 🤝" to Color(0xFFE65100)
        law.status.contains("Suspens", ignoreCase = true) -> "SUSPENSO" to Color(0xFF757575)
        else -> "EM ANDAMENTO ⚖️" to Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Apto ${law.apartment} • ${law.ownerName.uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Processo: ${law.processNumber.ifBlank { "Não informado" }} • Fórum: ${law.forum}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Dívida Ajuizada: R$ ${formatCurrency(law.totalDebt)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    if (law.successValue != null && law.successValue!! > 0) {
                        Text("Valor Recuperado: R$ ${formatCurrency(law.successValue!!)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Data de Ajuizamento: ${law.registrationDate}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Text("Andamentos Processuais (${item.progress.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (item.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    item.progress.forEach { prog ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("[${prog.date}]:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(prog.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawsuitsAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
    selectedForumFilter: String,
    onForumChange: (String) -> Unit,
    uniqueForums: List<String>,
    selectedValueRangeFilter: String,
    onValueRangeChange: (String) -> Unit,
    selectedPeriodFilter: String,
    onPeriodChange: (String) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    monthLabels: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Busca Avançada de Ajuizados") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status da Ação Judicial:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val statuses = listOf("Todos", "Em Andamento", "Concluído / Êxito", "Acordo Judicial", "Suspenso")
                    var expandedStatus by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedStatusFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            statuses.forEach { st ->
                                DropdownMenuItem(text = { Text(st) }, onClick = { onStatusChange(st); expandedStatus = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Fórum / Comarca:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var expandedForum by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedForum, onExpandedChange = { expandedForum = !expandedForum }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedForumFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o fórum") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedForum) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedForum, onDismissRequest = { expandedForum = false }) {
                            uniqueForums.forEach { f ->
                                DropdownMenuItem(text = { Text(f) }, onClick = { onForumChange(f); expandedForum = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Faixa de Valor Ajuizado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val ranges = listOf("Todos", "Até R$ 5.000", "R$ 5.000 a R$ 15.000", "Acima de R$ 15.000")
                    var expandedVal by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedVal, onExpandedChange = { expandedVal = !expandedVal }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedValueRangeFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione a faixa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVal) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedVal, onDismissRequest = { expandedVal = false }) {
                            ranges.forEach { r ->
                                DropdownMenuItem(text = { Text(r) }, onClick = { onValueRangeChange(r); expandedVal = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Período de Ajuizamento:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val periods = listOf("Todos", "Últimos 30 Dias", "Últimos 90 Dias", "Este Ano", "Mês/Ano Específico")
                    var expandedPeriod by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPeriodFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por período") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                            periods.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { onPeriodChange(p); expandedPeriod = false })
                            }
                        }
                    }

                    if (selectedPeriodFilter == "Mês/Ano Específico") {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expM by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expM = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expM, onDismissRequest = { expM = false }) {
                                    monthLabels.forEachIndexed { idx, m ->
                                        DropdownMenuItem(text = { Text(m) }, onClick = { onMonthChange(idx); expM = false })
                                    }
                                }
                            }

                            var expY by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expY = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ano: $selectedYear")
                                }
                                DropdownMenu(expanded = expY, onDismissRequest = { expY = false }) {
                                    (2024..2030).forEach { yr ->
                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = { onYearChange(yr); expY = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Aplicar Filtros")
            }
        }
    )
}

@Composable
fun LawsuitsReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório Jurídico") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount ação(ões) ajuizada(s). Próximo passo:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

// --- CADASTRO PROFISSIONAL DE UNIDADES ---

fun generateUnitsHtmlReport(units: List<UnitEntity>): String {
    val total = units.size
    val registered = units.count { it.ownerName.isNotBlank() }
    val pending = total - registered
    val occupancyRate = if (total > 0) (registered * 100 / total) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório de Cadastro de Unidades</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #1E88E5; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #1E88E5; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E3F2FD; border: 1px solid #BBDEFB; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1565C0; font-weight: bold; }
                .kpi-value { font-size: 16px; font-weight: bold; color: #0D47A1; margin-top: 4px; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #1E88E5; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-ok { background: #2E7D32; }
                .badge-pending { background: #E65100; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Cadastro Profissional & Consulta de Unidades</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Estrutura:</strong> 12 Unidades/Andar (A partir do 2º Andar) &nbsp;|&nbsp;
                <strong>Total Unidades:</strong> $total
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Unidades</div>
                    <div class="kpi-value">$total</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Cadastradas</div>
                    <div class="kpi-value">$registered</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Pendentes</div>
                    <div class="kpi-value">$pending</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Preenchimento</div>
                    <div class="kpi-value">$occupancyRate%</div>
                </div>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 6%;">#</th>
                        <th style="width: 12%;">Unidade</th>
                        <th style="width: 10%;">Andar</th>
                        <th style="width: 32%;">Nome Morador / Proprietário</th>
                        <th style="width: 20%;">Telefone / Celular</th>
                        <th style="width: 20%;">E-mail</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    units.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) })).forEachIndexed { idx, unit ->
        val name = if (unit.ownerName.isNotBlank()) unit.ownerName.uppercase() else "-"
        val phone = if (unit.phone.isNotBlank()) unit.phone else "-"
        val email = if (unit.email.isNotBlank()) unit.email else "-"

        html.append("""
            <tr>
                <td>${idx + 1}</td>
                <td><strong>Apto ${unit.apartment}</strong></td>
                <td>${unit.floor}º Andar</td>
                <td>$name</td>
                <td>$phone</td>
                <td>$email</td>
            </tr>
        """.trimIndent())
    }

    html.append("""
            </tbody>
        </table>

        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Administração Condominial</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Portaria / Recepção</strong><br>
                Consulta de Unidades
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateUnitsTextReport(units: List<UnitEntity>): String {
    val total = units.size
    val registered = units.count { it.ownerName.isNotBlank() }
    val pending = total - registered
    val occupancyRate = if (total > 0) (registered * 100 / total) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - CADASTRO DE UNIDADES\n")
    sb.append("📋 ESTRUTURA: 12 UNIDADES POR ANDAR (A PARTIR DO 2º ANDAR)\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO DE PREENCHIMENTO:\n")
    sb.append(" • Total de Unidades Mapeadas: $total\n")
    sb.append(" • Unidades com Cadastros Concluídos: $registered\n")
    sb.append(" • Unidades Pendentes: $pending\n")
    sb.append(" • Taxa de Preenchimento: $occupancyRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (units.isEmpty()) {
        sb.append("Nenhuma unidade cadastrada.\n")
    } else {
        units.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) })).forEachIndexed { idx, unit ->
            val status = if (unit.ownerName.isNotBlank()) "✅ CADASTRADO" else "⏳ PENDENTE"
            sb.append("[${idx + 1}] Apto ${unit.apartment} (${unit.floor}º Andar) | $status\n")
            sb.append("    Morador/Proprietário: ${if (unit.ownerName.isNotBlank()) unit.ownerName.uppercase() else "Não informado"}\n")
            sb.append("    Fone: ${unit.phone.ifBlank { "Não informado" }} | E-mail: ${unit.email.ifBlank { "Não informado" }}\n")
            if (unit.notes.isNotBlank()) {
                sb.append("    Obs: ${unit.notes}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Administração: ________________________\n")
    sb.append("Assinatura Portaria/Recepção: ____________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsRegistryScreen(dao: AppDao, context: Context) {
    val units by dao.getAllUnits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            deduplicateUnitsInDatabase(dao)

            if (dao.getUnitCount() == 0) {
                val defaultList = mutableListOf<UnitEntity>()
                for (floor in 2..15) {
                    for (u in 1..12) {
                        val apt = "${floor}${String.format(Locale.getDefault(), "%02d", u)}"
                        val fixedId = getUnitIdForApartment(apt)
                        defaultList.add(
                            UnitEntity(
                                id = fixedId,
                                apartment = apt,
                                floor = floor,
                                ownerName = "",
                                phone = "",
                                email = "",
                                notes = ""
                            )
                        )
                    }
                }
                dao.insertUnits(defaultList)
                defaultList.forEach { FirestoreSyncManager.syncUnit(it) }
            }
        }
    }

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedFloorFilter by remember { mutableStateOf("Todos Os Andares") }

    var unitToEdit by remember { mutableStateOf<UnitEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }

    val floorOptions = remember(units) {
        val floors = units.map { "${it.floor}º Andar" }.distinct().sortedBy {
            it.replace("º Andar", "").toIntOrNull() ?: 0
        }
        listOf("Todos Os Andares") + floors
    }

    val filteredUnits = remember(
        units, searchText, selectedStatusFilter, selectedFloorFilter
    ) {
        units.filter { unit ->
            val matchesStatus = when (selectedStatusFilter) {
                "Cadastrados" -> unit.ownerName.isNotBlank()
                "Pendentes" -> unit.ownerName.isBlank()
                else -> true
            }

            val matchesFloor = if (selectedFloorFilter == "Todos Os Andares") {
                true
            } else {
                val floorNum = selectedFloorFilter.replace("º Andar", "").toIntOrNull()
                unit.floor == floorNum
            }

            val fullText = "${unit.apartment} ${unit.ownerName} ${unit.phone} ${unit.email} ${unit.notes}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesFloor && matchesSearch
        }
    }

    val totalCount = units.size
    val registeredCount = units.count { it.ownerName.isNotBlank() }
    val pendingCount = totalCount - registeredCount
    val occupancyRate = if (totalCount > 0) (registeredCount * 100 / totalCount) else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Cadastro & Consulta de Unidades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Estrutura: 12 unidades por andar (A partir do 2º andar)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UnitMetricCard("Total Unidades", totalCount.toString(), Icons.Default.Apartment, Modifier.weight(1f))
                        UnitMetricCard("Cadastradas", registeredCount.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        UnitMetricCard("Pendentes", pendingCount.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f))
                        UnitMetricCard("Preenchimento", "$occupancyRate%", Icons.Default.Analytics, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por apto (ex: 201), nome, fone, e-mail...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedStatusFilter == "Todos",
                        onClick = { selectedStatusFilter = "Todos" },
                        label = { Text("Todos", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "Cadastrados",
                        onClick = { selectedStatusFilter = "Cadastrados" },
                        label = { Text("Cadastrados", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "Pendentes",
                        onClick = { selectedStatusFilter = "Pendentes" },
                        label = { Text("Pendentes", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }

                var expandedFloorMenu by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedFloorMenu,
                    onExpandedChange = { expandedFloorMenu = !expandedFloorMenu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFloorFilter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por Andar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloorMenu) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFloorMenu,
                        onDismissRequest = { expandedFloorMenu = false }
                    ) {
                        floorOptions.forEach { fl ->
                            DropdownMenuItem(
                                text = { Text(fl) },
                                onClick = {
                                    selectedFloorFilter = fl
                                    expandedFloorMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nova Unidade", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Imprimir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text(
                "Unidades Encontradas (${filteredUnits.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredUnits.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Nenhuma unidade encontrada para os filtros selecionados.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(filteredUnits.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) }))) { unit ->
                UnitCard(
                    unit = unit,
                    onEdit = { unitToEdit = unit },
                    context = context
                )
            }
        }
    }

    if (unitToEdit != null) {
        EditUnitDialog(
            unit = unitToEdit!!,
            onDismiss = { unitToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    dao.updateUnit(updated)
                    FirestoreSyncManager.syncUnit(updated)
                }
                unitToEdit = null
            }
        )
    }

    if (showAddDialog) {
        AddUnitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newUnit ->
                scope.launch {
                    dao.insertUnit(newUnit)
                    FirestoreSyncManager.syncUnit(newUnit)
                }
                showAddDialog = false
            }
        )
    }

    if (showPrintPreviewDialog) {
        val htmlContent = generateUnitsHtmlReport(filteredUnits)
        val textReport = generateUnitsTextReport(filteredUnits)

        UnitsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredUnits.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Cadastro_Unidades_CondSuites")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Cadastro de Unidades"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun UnitMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun UnitCard(
    unit: UnitEntity,
    onEdit: () -> Unit,
    context: Context
) {
    val isRegistered = unit.ownerName.isNotBlank()
    val statusColor = if (isRegistered) Color(0xFF2E7D32) else Color(0xFFE65100)
    val statusText = if (isRegistered) "CADASTRADO" else "PENDENTE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "APTO ${unit.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("${unit.floor}º ANDAR", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isRegistered) unit.ownerName else "Morador / Proprietário não cadastrado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isRegistered) MaterialTheme.colorScheme.onSurface else Color.Gray
            )

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("📞 Fone: ${unit.phone.ifBlank { "Não informado" }}", style = MaterialTheme.typography.bodySmall)
                    Text("✉️ E-mail: ${unit.email.ifBlank { "Não informado" }}", style = MaterialTheme.typography.bodySmall)
                    if (unit.notes.isNotBlank()) {
                        Text("📝 Obs: ${unit.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (unit.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${unit.phone}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Phone, "Ligar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                try {
                                    val clean = unit.phone.replace(Regex("[^0-9]"), "")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=55$clean"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                        }
                    }

                    if (unit.email.isNotBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${unit.email}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Email, "E-mail", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar Cadastro", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUnitDialog(
    unit: UnitEntity,
    onDismiss: () -> Unit,
    onConfirm: (UnitEntity) -> Unit
) {
    var ownerName by remember { mutableStateOf(unit.ownerName) }
    var phone by remember { mutableStateOf(unit.phone) }
    var email by remember { mutableStateOf(unit.email) }
    var notes by remember { mutableStateOf(unit.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastro do Apto ${unit.apartment} (${unit.floor}º Andar)") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nome do Morador / Proprietário *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / Celular (com DDD)") },
                    placeholder = { Text("(11) 99999-8888") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail do Condômino") },
                    placeholder = { Text("morador@email.com") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Gerais (Opcional)") },
                    placeholder = { Text("Ex: Vaga 42, Inquilino, Contato de emergência") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        unit.copy(
                            ownerName = ownerName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Cadastro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitDialog(
    onDismiss: () -> Unit,
    onConfirm: (UnitEntity) -> Unit
) {
    var apartment by remember { mutableStateOf("") }
    var floorText by remember { mutableStateOf("2") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Unidade") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Número (ex: 201) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Andar *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nome do Morador") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (apartment.isNotBlank()) {
                        val apt = apartment.trim()
                        val fl = floorText.toIntOrNull() ?: 2
                        val fixedId = getUnitIdForApartment(apt)
                        onConfirm(
                            UnitEntity(
                                id = fixedId,
                                apartment = apt,
                                floor = fl,
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun UnitsReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório de Cadastro") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório com $totalCount unidade(s). Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

// --- GESTÃO E RELATÓRIO PROFISSIONAL DE HORAS EXTRAS ---

fun generateOvertimeHtmlReport(
    overtimes: List<OvertimeEntity>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = overtimes.size
    val totalHours = overtimes.sumOf { it.totalHours }
    val approvedHours = overtimes.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }
    val pendingHours = overtimes.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val approvedCount = overtimes.count { it.status == "APROVADO" || it.status == "PAGO" }
    val pendingCount = overtimes.count { it.status == "PENDENTE" }
    val approvalRate = if (totalCount > 0) (approvedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Gerencial de Horas Extras</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #E65100; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #E65100; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #FFF3E0; border: 1px solid #FFE0B2; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #E65100; font-weight: bold; }
                .kpi-value { font-size: 16px; font-weight: bold; color: #BF360C; margin-top: 4px; }
                .section-title { font-size: 15px; color: #E65100; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #E65100; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-aprovado { background: #2E7D32; }
                .badge-pago { background: #0288D1; }
                .badge-pendente { background: #F57C00; }
                .badge-rejeitado { background: #C62828; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Gerencial e Controle de Horas Extras</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText &nbsp;|&nbsp;
                <strong>Total Registros:</strong> $totalCount
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Registros</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Acumuladas</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", totalHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Aprovadas</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", approvedHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Pendentes</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", pendingHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa Aprovação</div>
                    <div class="kpi-value">$approvalRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Controle de Jornada</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                O acompanhamento do banco de horas extras registra um total acumulado de <strong>${String.format(Locale.getDefault(), "%.1f", totalHours)} horas</strong> divididas em <strong>$totalCount solicitação(ões)</strong>.
                Atualmente, <strong>${String.format(Locale.getDefault(), "%.1f", approvedHours)} horas</strong> foram validadas/pagas ($approvedCount registros), enquanto <strong>${String.format(Locale.getDefault(), "%.1f", pendingHours)} horas</strong> aguardam deliberação do síndico/administração ($pendingCount pendentes).
                <br><strong>Recomendação de Gestão:</strong> Manter a auditoria das justificativas e motivos informados, auditando eventos decorrentes de manutenções emergenciais ou cobertura de turnos para controle orçamentário da folha de pagamento do condomínio.
            </p>

            <div class="section-title">📋 Detalhamento dos Lançamentos de Horas Extras</div>
    """.trimIndent())

    if (overtimes.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum lançamento de hora extra encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 20%;">Colaborador / Cargo</th>
                        <th style="width: 12%;">Data</th>
                        <th style="width: 14%;">Horário (Início-Fim)</th>
                        <th style="width: 8%;">Horas</th>
                        <th style="width: 28%;">Motivo da Hora Extra</th>
                        <th style="width: 14%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        overtimes.forEachIndexed { idx, item ->
            val badgeClass = when (item.status) {
                "APROVADO" -> "badge-aprovado"
                "PAGO" -> "badge-pago"
                "REJEITADO" -> "badge-rejeitado"
                else -> "badge-pendente"
            }

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>${item.employeeName.uppercase()}</strong><br><small style='color:#666'>${item.employeeRole}</small></td>
                    <td>${item.date}</td>
                    <td>${item.startTime} às ${item.endTime}</td>
                    <td><strong>${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h</strong></td>
                    <td>${item.reason}</td>
                    <td><span class="badge $badgeClass">${item.status}</span></td>
                </tr>
            """.trimIndent())
        }

        html.append("</tbody></table>")
    }

    html.append("""
        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Recursos Humanos / Gestão Pessoal</strong><br>
                Aprovação de Horas Extras
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateOvertimeTextReport(
    overtimes: List<OvertimeEntity>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = overtimes.size
    val totalHours = overtimes.sumOf { it.totalHours }
    val approvedHours = overtimes.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }
    val pendingHours = overtimes.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE HORAS EXTRAS\n")
    sb.append("📋 CONTROLE DE JORNADA & BANCO DE HORAS\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO DE JORNADA:\n")
    sb.append(" • Total de Solicitações: $totalCount\n")
    sb.append(" • Horas Totais Acumuladas: ${String.format(Locale.getDefault(), "%.1f", totalHours)}h\n")
    sb.append(" • Horas Aprovadas/Pagas: ${String.format(Locale.getDefault(), "%.1f", approvedHours)}h\n")
    sb.append(" • Horas Pendentes: ${String.format(Locale.getDefault(), "%.1f", pendingHours)}h\n")
    sb.append("--------------------------------------------------\n\n")

    if (overtimes.isEmpty()) {
        sb.append("Nenhum lançamento de hora extra encontrado para os filtros selecionados.\n")
    } else {
        overtimes.forEachIndexed { idx, item ->
            sb.append("[${idx + 1}] ${item.employeeName.uppercase()} (${item.employeeRole})\n")
            sb.append("    Data: ${item.date} | Horário: ${item.startTime} às ${item.endTime} (${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h)\n")
            sb.append("    Motivo: ${item.reason}\n")
            sb.append("    Status: ${item.status}\n")
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Colaborador: __________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeManagementScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
    val overtimes by dao.getAllOvertimeFlow().collectAsState(initial = emptyList())
    val context = LocalContext.current

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }

    var showAddDialog by remember { mutableStateOf(false) }
    var overtimeToEdit by remember { mutableStateOf<OvertimeEntity?>(null) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }

    val filteredList = remember(overtimes, searchText, selectedStatusFilter) {
        overtimes.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "Pendentes" -> item.status == "PENDENTE"
                "Aprovados" -> item.status == "APROVADO"
                "Pagos" -> item.status == "PAGO"
                "Rejeitados" -> item.status == "REJEITADO"
                else -> true
            }

            val fullText = "${item.employeeName} ${item.employeeRole} ${item.reason} ${item.date} ${item.status}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalHours = filteredList.sumOf { it.totalHours }
    val pendingHours = filteredList.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val approvedHours = filteredList.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Painel de Controle de Horas Extras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OvertimeMetricCard("Lançamentos", totalCount.toString(), Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Total Horas", "${String.format(Locale.getDefault(), "%.1f", totalHours)}h", Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Pendentes", "${String.format(Locale.getDefault(), "%.1f", pendingHours)}h", Icons.Default.NotificationImportant, Modifier.weight(1f))
                        OvertimeMetricCard("Aprovadas", "${String.format(Locale.getDefault(), "%.1f", approvedHours)}h", Icons.Default.CheckCircle, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nome, cargo ou motivo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pendentes", onClick = { selectedStatusFilter = "Pendentes" }, label = { Text("Pendentes", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Aprovados", onClick = { selectedStatusFilter = "Aprovados" }, label = { Text("Aprovados", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pagos", onClick = { selectedStatusFilter = "Pagos" }, label = { Text("Pagos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lançar Hora Extra", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Imprimir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Lançamentos de Horas Extras (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum lançamento de hora extra encontrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                OvertimeCard(
                    item = item,
                    currentUser = currentUser,
                    onEdit = { overtimeToEdit = item },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteOvertime(item.id)
                            FirestoreSyncManager.syncOvertime(item, isDelete = true)
                        }
                    },
                    onStatusChange = { newStatus ->
                        scope.launch(Dispatchers.IO) {
                            val updated = item.copy(status = newStatus, approvedByUsername = currentUser.username)
                            dao.updateOvertime(updated)
                            FirestoreSyncManager.syncOvertime(updated)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        RegisterOvertimeDialog(
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onConfirm = { newOvertime ->
                scope.launch(Dispatchers.IO) {
                    dao.insertOvertime(newOvertime)
                    FirestoreSyncManager.syncOvertime(newOvertime)
                    sendFcmPushNotification(
                        dao = dao,
                        title = "Nova Hora Extra Lançada",
                        body = "${newOvertime.employeeName} (${newOvertime.employeeRole}) - ${newOvertime.date} (${newOvertime.startTime} às ${newOvertime.endTime}, ${String.format(Locale.getDefault(), "%.1f", newOvertime.totalHours)}h)",
                        senderUsername = currentUser.username,
                        occurrenceId = newOvertime.id
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (overtimeToEdit != null) {
        EditOvertimeDialog(
            overtime = overtimeToEdit!!,
            currentUser = currentUser,
            onDismiss = { overtimeToEdit = null },
            onConfirm = { updated ->
                scope.launch(Dispatchers.IO) {
                    dao.updateOvertime(updated)
                    FirestoreSyncManager.syncOvertime(updated)
                }
                overtimeToEdit = null
            }
        )
    }

    if (showPrintPreviewDialog) {
        val htmlContent = generateOvertimeHtmlReport(filteredList, selectedStatusFilter, "Geral")
        val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, "Geral")

        OvertimeReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Horas_Extras_CondSuites")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Horas Extras"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsOvertimeScreen(dao: AppDao, context: Context) {
    val overtimes by dao.getAllOvertimeFlow().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val filteredList = remember(overtimes, searchText, selectedStatusFilter, selectedPeriodFilter, selectedMonth, selectedYear) {
        overtimes.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "Pendentes" -> item.status == "PENDENTE"
                "Aprovados" -> item.status == "APROVADO"
                "Pagos" -> item.status == "PAGO"
                "Rejeitados" -> item.status == "REJEITADO"
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.date) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val diffDays = (System.currentTimeMillis() - date.time) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val fullText = "${item.employeeName} ${item.employeeRole} ${item.reason} ${item.date}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesPeriod && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalHours = filteredList.sumOf { it.totalHours }
    val pendingHours = filteredList.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val approvedHours = filteredList.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Relatório Analítico de Horas Extras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OvertimeMetricCard("Registros", totalCount.toString(), Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Horas Totais", "${String.format(Locale.getDefault(), "%.1f", totalHours)}h", Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Aprovadas", "${String.format(Locale.getDefault(), "%.1f", approvedHours)}h", Icons.Default.CheckCircle, Modifier.weight(1f))
                        OvertimeMetricCard("Pendentes", "${String.format(Locale.getDefault(), "%.1f", pendingHours)}h", Icons.Default.NotificationImportant, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nome, cargo ou motivo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pendentes", onClick = { selectedStatusFilter = "Pendentes" }, label = { Text("Pendentes", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Aprovados", onClick = { selectedStatusFilter = "Aprovados" }, label = { Text("Aprovados", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pagos", onClick = { selectedStatusFilter = "Pagos" }, label = { Text("Pagos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir Relatório (PDF)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
                            val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Horas Extras"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Registros Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                OvertimeCard(
                    item = item,
                    currentUser = UserEntity(role = "PORTEIRO"),
                    onEdit = {},
                    onDelete = {},
                    onStatusChange = {}
                )
            }
        }
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateOvertimeHtmlReport(filteredList, selectedStatusFilter, periodText)
        val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, periodText)

        OvertimeReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Horas_Extras_CondSuites")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Horas Extras"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun OvertimeMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun OvertimeCard(
    item: OvertimeEntity,
    currentUser: UserEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val (statusText, statusColor) = when (item.status) {
        "APROVADO" -> "APROVADO ✅" to Color(0xFF2E7D32)
        "PAGO" -> "PAGO 💰" to Color(0xFF0288D1)
        "REJEITADO" -> "REJEITADO ❌" to Color(0xFFC62828)
        else -> "PENDENTE ⏳" to Color(0xFFE65100)
    }

    val canApprove = currentUser.role == "ADMIN" || currentUser.role == "Síndico"
    val isWithin15Min = isOvertimeEditable(item.registrationDate)
    val canEditOvertime = if (canApprove) true else isWithin15Min

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.employeeName.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(item.employeeRole, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("📅 Data: ${item.date} • Horário: ${item.startTime} às ${item.endTime} (${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Motivo da Hora Extra:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(item.reason, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (item.approvedByUsername.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Aprovado/Atualizado por: ${item.approvedByUsername}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (canApprove) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (item.status != "APROVADO") {
                            TextButton(onClick = { onStatusChange("APROVADO") }) {
                                Text("Aprovar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                            }
                        }
                        if (item.status != "PAGO") {
                            TextButton(onClick = { onStatusChange("PAGO") }) {
                                Text("Pagar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0288D1))
                            }
                        }
                        if (item.status != "REJEITADO") {
                            TextButton(onClick = { onStatusChange("REJEITADO") }) {
                                Text("Rejeitar", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canEditOvertime) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOvertimeDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (OvertimeEntity) -> Unit
) {
    var employeeName by remember { mutableStateOf(currentUser.username) }
    var employeeRole by remember { mutableStateOf(if (currentUser.role.isNotBlank()) currentUser.role else "Zelador") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var reason by remember { mutableStateOf("") }
    var expandedRole by remember { mutableStateOf(false) }

    val roles = listOf("Zelador", "Porteiro", "Manutenção", "Limpeza", "Administração", "Síndico")
    val totalHours = calculateOvertimeHours(startTime, endTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lançar Hora Extra") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Nome do Colaborador *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = employeeRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo / Função *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { employeeRole = r; expandedRole = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Data (DD/MM/AAAA) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Início (ex: 18:00) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fim (ex: 21:00) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Total Calculado: ${String.format(Locale.getDefault(), "%.1f", totalHours)} hora(s)",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo Detalhado da Hora Extra *") },
                    placeholder = { Text("Ex: Acompanhamento emergencial da equipe técnica de manutenção do elevador social no período noturno...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (employeeName.isNotBlank() && reason.isNotBlank()) {
                        onConfirm(
                            OvertimeEntity(
                                id = System.currentTimeMillis(),
                                employeeName = employeeName.trim(),
                                employeeRole = employeeRole.trim(),
                                date = dateText.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                totalHours = totalHours,
                                reason = reason.trim(),
                                status = "PENDENTE",
                                approvedByUsername = ""
                            )
                        )
                    }
                },
                enabled = employeeName.isNotBlank() && reason.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar Lançamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOvertimeDialog(
    overtime: OvertimeEntity,
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (OvertimeEntity) -> Unit
) {
    var employeeName by remember { mutableStateOf(overtime.employeeName) }
    var employeeRole by remember { mutableStateOf(overtime.employeeRole) }
    var dateText by remember { mutableStateOf(overtime.date) }
    var startTime by remember { mutableStateOf(overtime.startTime) }
    var endTime by remember { mutableStateOf(overtime.endTime) }
    var reason by remember { mutableStateOf(overtime.reason) }
    var status by remember { mutableStateOf(overtime.status) }
    var expandedRole by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val roles = listOf("Zelador", "Porteiro", "Manutenção", "Limpeza", "Administração", "Síndico")
    val statuses = listOf("PENDENTE", "APROVADO", "PAGO", "REJEITADO")
    val totalHours = calculateOvertimeHours(startTime, endTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Hora Extra") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Nome do Colaborador *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = employeeRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo / Função *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { employeeRole = r; expandedRole = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Início *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fim *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status do Lançamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statuses.forEach { st ->
                            DropdownMenuItem(text = { Text(st) }, onClick = { status = st; expandedStatus = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo Detalhado *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        overtime.copy(
                            employeeName = employeeName.trim(),
                            employeeRole = employeeRole.trim(),
                            date = dateText.trim(),
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            totalHours = totalHours,
                            reason = reason.trim(),
                            status = status,
                            approvedByUsername = currentUser.username
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun OvertimeReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório de Horas Extras") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount registro(s). Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}



fun printHtmlReport(context: Context, htmlContent: String, jobName: String = "Relatorio_Manutencao_Elevadores") {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun isElevatorOccurrence(item: OccurrenceWithMessages): Boolean {
    val fullText = "${item.occurrence.title} ${item.occurrence.occurrenceType} ${item.occurrence.apartment}".lowercase()
    return fullText.contains("elevador") || fullText.contains("elevadores") ||
           fullText.contains("social") || fullText.contains("serviço") || fullText.contains("servico")
}

fun getElevatorTypeForOccurrence(item: OccurrenceWithMessages): String {
    val fullText = "${item.occurrence.title} ${item.occurrence.occurrenceType} ${item.occurrence.apartment} ${item.messages.joinToString(" ") { it.message.text }}".lowercase()
    val isSocial = fullText.contains("social")
    val isService = fullText.contains("serviço") || fullText.contains("servico")
    
    return when {
        isSocial && !isService -> "Elevador Social"
        isService && !isSocial -> "Elevador Serviço"
        isSocial && isService -> "Elevador Social e Serviço"
        else -> "Elevador (Geral)"
    }
}

fun generateElevatorMaintenanceHtmlReport(
    occurrences: List<OccurrenceWithMessages>,
    filterElevatorText: String,
    periodText: String,
    statusText: String,
    urgencyText: String
): String {
    val totalCount = occurrences.size
    val socialCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val openCount = occurrences.count { it.occurrence.status == "ABERTA" }
    val closedCount = occurrences.count { it.occurrence.status == "FINALIZADA" }
    val urgentCount = occurrences.count { it.occurrence.isUrgent }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório de Manutenção dos Elevadores</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #1E88E5; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #1E88E5; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E3F2FD; border: 1px solid #BBDEFB; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1565C0; font-weight: bold; }
                .kpi-value { font-size: 18px; font-weight: bold; color: #0D47A1; margin-top: 4px; }
                .section-title { font-size: 15px; color: #1565C0; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #1E88E5; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-social { background: #1976D2; }
                .badge-service { background: #E65100; }
                .badge-general { background: #757575; }
                .badge-open { background: #0288D1; }
                .badge-closed { background: #388E3C; }
                .badge-urgent { background: #D32F2F; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Técnico de Manutenção & Ocorrências dos Elevadores</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Filtro Elevador:</strong> $filterElevatorText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText<br>
                <strong>Status:</strong> $statusText &nbsp;|&nbsp; 
                <strong>Urgência:</strong> $urgencyText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Chamados</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Elev. Social</div>
                    <div class="kpi-value">$socialCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Elev. Serviço</div>
                    <div class="kpi-value">$serviceCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Urgentes</div>
                    <div class="kpi-value">$urgentCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Solucionadas</div>
                    <div class="kpi-value">$resRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise de Manutenção e Diagnóstico</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                Relatório analítico do histórico operacional dos elevadores para controle da gestão predial e acompanhamento da empresa conservadora de elevadores.
                ${if (socialCount > serviceCount) "Maior incidência observada no <strong>Elevador Social</strong> ($socialCount ocorrências)." else if (serviceCount > socialCount) "Maior incidência observada no <strong>Elevador de Serviço</strong> ($serviceCount ocorrências)." else "Volume de chamados equivalente entre Elevador Social e de Serviço ($socialCount chamados cada)."}
                Taxa de resolução atual de <strong>$resRate%</strong> ($closedCount finalizadas e $openCount em aberto de $totalCount registradas).
            </p>

            <div class="section-title">📋 Detalhamento das Ocorrências</div>
    """.trimIndent())

    if (occurrences.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhuma ocorrência registrada com os filtros aplicados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 16%;">Elevador</th>
                        <th style="width: 11%;">Data</th>
                        <th style="width: 28%;">Assunto / Motivo</th>
                        <th style="width: 11%;">Status</th>
                        <th style="width: 11%;">Prioridade</th>
                        <th style="width: 19%;">Solicitante / Local</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        occurrences.forEachIndexed { idx, item ->
            val type = getElevatorTypeForOccurrence(item)
            val badgeClass = when (type) {
                "Elevador Social" -> "badge-social"
                "Elevador Serviço" -> "badge-service"
                else -> "badge-general"
            }
            val statusClass = if (item.occurrence.status == "FINALIZADA") "badge-closed" else "badge-open"
            val urgentBadge = if (item.occurrence.isUrgent) "<span class='badge badge-urgent'>URGENTE</span>" else "Normal"
            val msgCount = item.messages.size

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><span class="badge $badgeClass">$type</span></td>
                    <td>${item.occurrence.date}</td>
                    <td><strong>${item.occurrence.title}</strong></td>
                    <td><span class="badge $statusClass">${item.occurrence.status}</span></td>
                    <td>$urgentBadge</td>
                    <td>${item.occurrence.createdByUsername}<br><small>Local: ${item.occurrence.apartment} ($msgCount msgs)</small></td>
                </tr>
            """.trimIndent())
        }

        html.append("</tbody></table>")
    }

    html.append("""
        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Técnico / Empresa Conservadora</strong><br>
                Manutenção dos Elevadores
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateElevatorMaintenanceTextReport(
    occurrences: List<OccurrenceWithMessages>,
    filterElevatorText: String,
    periodText: String
): String {
    val totalCount = occurrences.size
    val socialCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val openCount = occurrences.count { it.occurrence.status == "ABERTA" }
    val closedCount = occurrences.count { it.occurrence.status == "FINALIZADA" }
    val urgentCount = occurrences.count { it.occurrence.isUrgent }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE ELEVADORES\n")
    sb.append("📋 ELEVADOR SOCIAL & ELEVADOR DE SERVIÇO\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Elevador: $filterElevatorText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO TÉCNICO / MANUTENÇÃO:\n")
    sb.append(" • Total de Chamados: $totalCount\n")
    sb.append(" • Elevador Social: $socialCount | Elevador Serviço: $serviceCount\n")
    sb.append(" • Chamados Urgentes: $urgentCount\n")
    sb.append(" • Status: $closedCount Concluídas / $openCount Em Aberto ($resRate% Solucionadas)\n")
    sb.append("--------------------------------------------------\n\n")

    if (occurrences.isEmpty()) {
        sb.append("Nenhuma ocorrência encontrada para os filtros selecionados.\n")
    } else {
        occurrences.forEachIndexed { idx, item ->
            val type = getElevatorTypeForOccurrence(item)
            val occ = item.occurrence
            sb.append("[${idx + 1}] $type | Data: ${occ.date}\n")
            sb.append("    Assunto: ${occ.title}\n")
            sb.append("    Status: ${occ.status} | Urgência: ${if (occ.isUrgent) "🚨 URGENTE" else "Normal"}\n")
            sb.append("    Solicitante: ${occ.createdByUsername} (Local: ${occ.apartment})\n")
            if (item.messages.isNotEmpty()) {
                val lastMsg = item.messages.last().message
                sb.append("    Última Interação: [${lastMsg.senderUsername}] ${lastMsg.text}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Téc. Elevadores: ______________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsElevatorOccurrencesScreen(dao: AppDao, context: Context) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())

    // Advanced search filters state
    var searchText by remember { mutableStateOf("") }
    var selectedElevatorFilter by remember { mutableStateOf("Todos") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedUrgencyFilter by remember { mutableStateOf("Todas") }
    var selectedComponentFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val allElevatorOccurrences = remember(occurrences) {
        occurrences.filter { isElevatorOccurrence(it) }
    }

    val filteredList = remember(
        allElevatorOccurrences, searchText, selectedElevatorFilter, selectedStatusFilter,
        selectedUrgencyFilter, selectedComponentFilter, selectedPeriodFilter, selectedMonth, selectedYear
    ) {
        allElevatorOccurrences.filter { item ->
            val type = getElevatorTypeForOccurrence(item)

            val matchesElevator = when (selectedElevatorFilter) {
                "Elevador Social" -> type == "Elevador Social"
                "Elevador Serviço" -> type == "Elevador Serviço"
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "ABERTA" -> item.occurrence.status == "ABERTA"
                "EM_ESPERA" -> item.occurrence.status == "EM_ESPERA"
                "FINALIZADA" -> item.occurrence.status == "FINALIZADA"
                else -> true
            }

            val matchesUrgency = when (selectedUrgencyFilter) {
                "Apenas Urgentes" -> item.occurrence.isUrgent
                "Normais" -> !item.occurrence.isUrgent
                else -> true
            }

            val fullContent = "${item.occurrence.title} ${item.messages.joinToString(" ") { it.message.text }}".lowercase()
            val matchesComponent = when (selectedComponentFilter) {
                "Porta/Dictador" -> fullContent.contains("porta") || fullContent.contains("dictador") || fullContent.contains("tranco")
                "Botões/Painel" -> fullContent.contains("bot") || fullContent.contains("painel") || fullContent.contains("chamada")
                "Barulho/Vibração" -> fullContent.contains("barulho") || fullContent.contains("ruido") || fullContent.contains("vibra")
                "Parada/Retenção" -> fullContent.contains("parad") || fullContent.contains("retenc") || fullContent.contains("nivel")
                "Placa/Comando" -> fullContent.contains("placa") || fullContent.contains("comando") || fullContent.contains("eletri")
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.occurrence.date) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val itemMillis = date.time
                    val nowMillis = System.currentTimeMillis()
                    val diffDays = (nowMillis - itemMillis) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val matchesSearch = searchText.isBlank() ||
                item.occurrence.title.contains(searchText, ignoreCase = true) ||
                item.occurrence.apartment.contains(searchText, ignoreCase = true) ||
                item.occurrence.createdByUsername.contains(searchText, ignoreCase = true) ||
                item.messages.any { it.message.text.contains(searchText, ignoreCase = true) }

            matchesElevator && matchesStatus && matchesUrgency && matchesComponent && matchesPeriod && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val socialCount = filteredList.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = filteredList.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val urgentCount = filteredList.count { it.occurrence.isUrgent }
    val closedCount = filteredList.count { it.occurrence.status == "FINALIZADA" }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0

    val activeFilterCount = (if (selectedElevatorFilter != "Todos") 1 else 0) +
            (if (selectedStatusFilter != "Todos") 1 else 0) +
            (if (selectedUrgencyFilter != "Todas") 1 else 0) +
            (if (selectedComponentFilter != "Todos") 1 else 0) +
            (if (selectedPeriodFilter != "Todos") 1 else 0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Painel de Indicadores de Manutenção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Total Chamados", totalCount.toString(), Icons.Default.Elevator, Modifier.weight(1f))
                        ElevatorMetricCard("Elev. Social", socialCount.toString(), Icons.Default.Groups, Modifier.weight(1f))
                        ElevatorMetricCard("Elev. Serviço", serviceCount.toString(), Icons.Default.Build, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Urgentes 🚨", urgentCount.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f))
                        ElevatorMetricCard("Taxa Solução", "$resRate%", Icons.Default.CheckCircle, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAdvancedFiltersDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filtros ${if (activeFilterCount > 0) "($activeFilterCount)" else ""}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedElevatorFilter == "Todos",
                        onClick = { selectedElevatorFilter = "Todos" },
                        label = { Text("Todos Elevadores") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Elevador Social",
                        onClick = { selectedElevatorFilter = "Elevador Social" },
                        label = { Text("Social") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Elevador Serviço",
                        onClick = { selectedElevatorFilter = "Elevador Serviço" },
                        label = { Text("Serviço") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Filtros ativos aplicados ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedElevatorFilter = "Todos"
                            selectedStatusFilter = "Todos"
                            selectedUrgencyFilter = "Todas"
                            selectedComponentFilter = "Todos"
                            selectedPeriodFilter = "Todos"
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar Filtros", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir Relatório (PDF)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
                            val textReport = generateElevatorMaintenanceTextReport(filteredList, selectedElevatorFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Elevadores"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Ocorrências Registradas (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma ocorrência encontrada para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                val type = getElevatorTypeForOccurrence(item)
                val isExpanded = expandedCardId == item.occurrence.id

                ElevatorOccurrenceCard(
                    item = item,
                    elevatorType = type,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.occurrence.id
                    },
                    onShare = {
                        shareOccurrenceReport(context, item)
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        ElevatorAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedUrgencyFilter = selectedUrgencyFilter,
            onUrgencyChange = { selectedUrgencyFilter = it },
            selectedComponentFilter = selectedComponentFilter,
            onComponentChange = { selectedComponentFilter = it },
            selectedPeriodFilter = selectedPeriodFilter,
            onPeriodChange = { selectedPeriodFilter = it },
            selectedMonth = selectedMonth,
            onMonthChange = { selectedMonth = it },
            selectedYear = selectedYear,
            onYearChange = { selectedYear = it },
            monthLabels = monthLabels,
            onDismiss = { showAdvancedFiltersDialog = false }
        )
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateElevatorMaintenanceHtmlReport(filteredList, selectedElevatorFilter, periodText, selectedStatusFilter, selectedUrgencyFilter)
        val textReport = generateElevatorMaintenanceTextReport(filteredList, selectedElevatorFilter, periodText)

        ElevatorReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Manutencao_Elevadores")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Elevadores"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun ElevatorMetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ElevatorOccurrenceCard(
    item: OccurrenceWithMessages,
    elevatorType: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val badgeColor = when (elevatorType) {
        "Elevador Social" -> Color(0xFF1976D2)
        "Elevador Serviço" -> Color(0xFFE65100)
        else -> Color(0xFF616161)
    }

    val statusColor = when (item.occurrence.status) {
        "FINALIZADA" -> Color(0xFF2E7D32)
        "EM_ESPERA" -> Color(0xFFF57C00)
        else -> Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        elevatorType.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            item.occurrence.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.occurrence.isUrgent) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "🚨 URGENTE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(item.occurrence.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    "Local: ${item.occurrence.apartment} • Data: ${item.occurrence.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Autor: ${item.occurrence.createdByUsername}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text("Histórico de Interações (${item.messages.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (item.messages.isEmpty()) {
                    Text("Nenhuma mensagem registrada.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    item.messages.forEach { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("[${msg.message.date}] ${msg.message.senderUsername}:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(msg.message.text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatorAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
    selectedUrgencyFilter: String,
    onUrgencyChange: (String) -> Unit,
    selectedComponentFilter: String,
    onComponentChange: (String) -> Unit,
    selectedPeriodFilter: String,
    onPeriodChange: (String) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    monthLabels: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Busca Avançada de Ocorrências") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status do Chamado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedStatusFilter == "Todos", onClick = { onStatusChange("Todos") }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedStatusFilter == "ABERTA", onClick = { onStatusChange("ABERTA") }, label = { Text("Abertas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedStatusFilter == "FINALIZADA", onClick = { onStatusChange("FINALIZADA") }, label = { Text("Fechadas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }

                Column {
                    Text("Urgência:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedUrgencyFilter == "Todas", onClick = { onUrgencyChange("Todas") }, label = { Text("Todas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedUrgencyFilter == "Apenas Urgentes", onClick = { onUrgencyChange("Apenas Urgentes") }, label = { Text("Urgentes 🚨", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedUrgencyFilter == "Normais", onClick = { onUrgencyChange("Normais") }, label = { Text("Normais", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }

                Column {
                    Text("Componente / Motivo da Falha:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val components = listOf("Todos", "Porta/Dictador", "Botões/Painel", "Barulho/Vibração", "Parada/Retenção", "Placa/Comando")
                    var expandedComp by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedComp, onExpandedChange = { expandedComp = !expandedComp }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedComponentFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o motivo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedComp) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedComp, onDismissRequest = { expandedComp = false }) {
                            components.forEach { comp ->
                                DropdownMenuItem(text = { Text(comp) }, onClick = { onComponentChange(comp); expandedComp = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Período de Análise:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val periods = listOf("Todos", "Últimos 30 Dias", "Últimos 90 Dias", "Este Ano", "Mês/Ano Específico")
                    var expandedPeriod by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPeriodFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por período") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                            periods.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { onPeriodChange(p); expandedPeriod = false })
                            }
                        }
                    }

                    if (selectedPeriodFilter == "Mês/Ano Específico") {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expM by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expM = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expM, onDismissRequest = { expM = false }) {
                                    monthLabels.forEachIndexed { idx, m ->
                                        DropdownMenuItem(text = { Text(m) }, onClick = { onMonthChange(idx); expM = false })
                                    }
                                }
                            }

                            var expY by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expY = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ano: $selectedYear")
                                }
                                DropdownMenu(expanded = expY, onDismissRequest = { expY = false }) {
                                    (2024..2030).forEach { yr ->
                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = { onYearChange(yr); expY = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Aplicar Filtros")
            }
        }
    )
}

@Composable
fun ElevatorReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório Profissional") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount ocorrência(s). Próximo passo:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
fun ReportsOccurrencesScreen(dao: AppDao, context: Context) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfessionalDialog by remember { mutableStateOf(false) }
    var previewText by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("Relatório de Ocorrências") }
    val tabs = listOf("Abertas", "Fechadas", "Elevadores")
    
    // Filter states for Elevators
    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }
    var isYearly by remember { mutableStateOf(false) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val openOccurrences = occurrences.filter { it.occurrence.status == "ABERTA" }
    val closedOccurrences = occurrences.filter { it.occurrence.status == "FINALIZADA" }
    val elevatorOccurrences = occurrences.filter { item ->
        val isElevator = item.occurrence.title.contains("Elevador", ignoreCase = true) || 
                       item.occurrence.occurrenceType == "Elevador" ||
                       item.occurrence.apartment == "CONDOMÍNIO"
        val date = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.occurrence.date)
        } catch (_: Exception) { null }
        val cal = Calendar.getInstance()
        if (date != null) cal.time = date
        val matchesYear = cal.get(Calendar.YEAR) == selectedYear
        val matchesMonth = isYearly || cal.get(Calendar.MONTH) == selectedMonth
        isElevator && matchesYear && matchesMonth
    }

    val filtered = when (selectedTab) {
        0 -> openOccurrences
        1 -> closedOccurrences
        2 -> elevatorOccurrences
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Relatórios de Ocorrências", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }

        // Section for quick access & share of reports
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Central de Relatórios e Compartilhamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))

                    // Ocorrências Abertas row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ocorrências Abertas (${openOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de chamados abertos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 0 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS ABERTAS", openOccurrences)
                                previewTitle = "Relatório de Ocorrências Abertas"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Abertas", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    // Ocorrências Fechadas row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ocorrências Fechadas (${closedOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de chamados finalizados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 1 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS FECHADAS", closedOccurrences)
                                previewTitle = "Relatório de Ocorrências Fechadas"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Fechadas", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    // Ocorrências em Elevadores row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Elevadores (${if (isYearly) "Anual: $selectedYear" else "Mensal: ${monthLabels[selectedMonth]}/$selectedYear"} - ${elevatorOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de ocorrências em elevadores", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 2 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                val period = if (isYearly) "ANUAL ($selectedYear)" else "MENSAIS (${monthLabels[selectedMonth]}/$selectedYear)"
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS EM ELEVADORES - $period", elevatorOccurrences)
                                previewTitle = "Relatório de Ocorrências em Elevadores"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Elevadores", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { showProfessionalDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Relatório Profissional de Impressão (Filtros Avançados)")
                    }
                }
            }
        }

        item {
            Text("Visualização Detalhada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Column {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
                
                if (selectedTab == 2) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Visão:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = !isYearly,
                            onClick = { isYearly = false },
                            label = { Text("Mensal") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isYearly,
                            onClick = { isYearly = true },
                            label = { Text("Anual") }
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (!isYearly) {
                            var expandedMonth by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expandedMonth = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                                    monthLabels.forEachIndexed { index, label ->
                                        DropdownMenuItem(text = { Text(label) }, onClick = { selectedMonth = index; expandedMonth = false })
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        
                        var expandedYear by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { expandedYear = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Ano: $selectedYear")
                            }
                            DropdownMenu(expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
                                (2024..2030).forEach { year ->
                                    DropdownMenuItem(text = { Text(year.toString()) }, onClick = { selectedYear = year; expandedYear = false })
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma ocorrência encontrada nesta categoria.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filtered) { item ->
                ReportItemCard(
                    title = item.occurrence.title,
                    subtitle = "Local: ${item.occurrence.apartment} | Status: ${item.occurrence.status}",
                    value = "-",
                    date = item.occurrence.date
                ) {
                    shareOccurrenceReport(context, item)
                }
            }
        }
    }
}

@Composable
fun ReportTypeDialog(onDismiss: () -> Unit, onSelect: (Boolean) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tipo de Relatório") }, text = { Text("Deseja um relatório resumido ou detalhado?") },
        confirmButton = { Button({ onSelect(true) }) { Text("Detalhado") } }, dismissButton = { TextButton({ onSelect(false) }) { Text("Resumido") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalReportDialog(dao: AppDao, context: Context, onDismiss: () -> Unit) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    
    var filterLocationType by remember { mutableStateOf("Todos") }
    var specificApto by remember { mutableStateOf("") }
    var filterSubject by remember { mutableStateOf("Todos") }
    var filterUrgent by remember { mutableStateOf("Todos") }
    var filterDate by remember { mutableStateOf("") }

    val allSubjects = remember(occurrences) { listOf("Todos") + occurrences.map { it.occurrence.title }.distinct().sorted() }
    var expandedSubject by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Relatório Profissional de Ocorrências") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escolha os filtros para impressão e exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Column {
                    Text("Local:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = filterLocationType == "Todos",
                            onClick = { filterLocationType = "Todos" },
                            label = { Text("Todos", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterLocationType == "Unidade Específica",
                            onClick = { filterLocationType = "Unidade Específica" },
                            label = { Text("Unidade", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterLocationType == "Condomínio",
                            onClick = { filterLocationType = "Condomínio" },
                            label = { Text("Condomínio", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (filterLocationType == "Unidade Específica") {
                    OutlinedTextField(
                        value = specificApto,
                        onValueChange = { specificApto = it },
                        label = { Text("Nº do Apartamento / Unidade") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedSubject,
                    onExpandedChange = { expandedSubject = !expandedSubject },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = filterSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assunto / Título") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubject,
                        onDismissRequest = { expandedSubject = false }
                    ) {
                        allSubjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    filterSubject = subj
                                    expandedSubject = false
                                }
                            )
                        }
                    }
                }

                Column {
                    Text("Urgência:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = filterUrgent == "Todos",
                            onClick = { filterUrgent = "Todos" },
                            label = { Text("Todas", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterUrgent == "Apenas Urgentes",
                            onClick = { filterUrgent = "Apenas Urgentes" },
                            label = { Text("Apenas Urgentes", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterUrgent == "Apenas Normais",
                            onClick = { filterUrgent = "Apenas Normais" },
                            label = { Text("Apenas Normais", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = filterDate,
                    onValueChange = { filterDate = it },
                    label = { Text("Data (ex: 14/09/2026 ou vazio para todas)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filteredList = occurrences.filter { item ->
                        val matchesLocation = when (filterLocationType) {
                            "Unidade Específica" -> item.occurrence.apartment.equals(specificApto.trim(), ignoreCase = true)
                            "Condomínio" -> item.occurrence.apartment.equals("CONDOMÍNIO", ignoreCase = true)
                            else -> true
                        }
                        val matchesSubject = filterSubject == "Todos" || item.occurrence.title.equals(filterSubject, ignoreCase = true)
                        val matchesUrgency = when (filterUrgent) {
                            "Apenas Urgentes" -> item.occurrence.isUrgent
                            "Apenas Normais" -> !item.occurrence.isUrgent
                            else -> true
                        }
                        val matchesDate = filterDate.isBlank() || item.occurrence.date.contains(filterDate.trim())

                        matchesLocation && matchesSubject && matchesUrgency && matchesDate
                    }

                    val report = StringBuilder()
                    report.append("==================================================\n")
                    report.append("🏢 CONDSUITES - RELATÓRIO PROFISSIONAL DE OCORRÊNCIAS\n")
                    report.append("==================================================\n")
                    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
                    report.append("--------------------------------------------------\n")
                    report.append("FILTROS SELECIONADOS:\n")
                    report.append("- Local: $filterLocationType ${if (filterLocationType == "Unidade Específica") "(Apto: $specificApto)" else ""}\n")
                    report.append("- Assunto: $filterSubject\n")
                    report.append("- Urgência: $filterUrgent\n")
                    report.append("- Data: ${if (filterDate.isBlank()) "Todas" else filterDate}\n")
                    report.append("Total de Ocorrências: ${filteredList.size}\n")
                    report.append("==================================================\n\n")

                    if (filteredList.isEmpty()) {
                        report.append("Nenhuma ocorrência encontrada com os filtros selecionados.\n")
                    } else {
                        filteredList.forEachIndexed { index, item ->
                            report.append("[${index + 1}] ID #${item.occurrence.id} | Local: ${item.occurrence.apartment}\n")
                            report.append("Assunto: ${item.occurrence.title}\n")
                            report.append("Data: ${item.occurrence.date} | Status: ${item.occurrence.status}\n")
                            report.append("Prioridade: ${if (item.occurrence.isUrgent) "🚨 URGENTE" else "Normal"}\n")
                            report.append("Criado por: ${item.occurrence.createdByUsername}\n")
                            if (item.messages.isNotEmpty()) {
                                report.append("Mensagens/Interações: ${item.messages.size}\n")
                            }
                            report.append("--------------------------------------------------\n")
                        }
                    }

                    report.append("\n==================================================\n")
                    report.append("Assinatura do Responsável: ________________________\n")
                    report.append("==================================================\n")

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report.toString())
                    }
                    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Profissional"))
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Description, null)
                Spacer(Modifier.width(8.dp))
                Text("Gerar e Imprimir Relatório")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ReportItemCard(title: String, subtitle: String, value: String, date: String, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall); Text("Valor: R$ $value | Data: $date", style = MaterialTheme.typography.labelSmall) }
            IconButton(onShare) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun HomeScreen(dao: AppDao, currentUser: UserEntity, onNavigate: (Screen) -> Unit) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    
    val filteredOccurrences = occurrences.filter { 
        if (currentUser.role == "Zelador") {
            it.occurrence.type == "GERAL"
        } else {
            true // Others see everything or are filtered by Screen visibility
        }
    }
    
    val openOccurrences = filteredOccurrences.filter { it.occurrence.status == "ABERTA" }
    val waitingOccurrences = filteredOccurrences.filter { it.occurrence.status == "EM_ESPERA" }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Painel Administrativo", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        
        Text("Resumo de Ocorrências", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "Abertas",
                count = openOccurrences.size,
                color = Color(0xFF2196F3),
                icon = Icons.Default.ChatBubbleOutline,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Occurrences) }
            )
            SummaryCard(
                title = "Em Espera",
                count = waitingOccurrences.size,
                color = Color(0xFFFF9800),
                icon = Icons.Default.PauseCircleOutline,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Occurrences) }
            )
        }
        
        if (openOccurrences.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Últimas Ocorrências Abertas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            openOccurrences.take(3).forEach { occWithMsgs ->
                val occ = occWithMsgs.occurrence
                val isCondo = occ.apartment == "CONDOMÍNIO"
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNavigate(Screen.Occurrences) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCondo) Color(0xFFE8F4FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isCondo) Icons.Default.Apartment else Icons.Default.NotificationImportant, null, tint = Color(0xFF2196F3))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(occ.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(if (isCondo) "🏢 CONDOMÍNIO • ${occ.date}" else "🚪 Unidade ${occ.apartment} • ${occ.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, count: Int, color: Color, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun PlaceholderScreen(screen: Screen) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(screen.icon, null, Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)); Text("Módulo: ${screen.title}", style = MaterialTheme.typography.headlineMedium)
    }
}

// --- Acordos Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsManagementScreen(
    agreements: List<AgreementWithInstallments>, 
    delinquents: List<DelinquentWithProgress>, 
    lawsuits: List<LawsuitWithProgress>, 
    dao: AppDao, 
    scope: CoroutineScope,
    currentUser: UserEntity,
    isCompact: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Notificações", "Acordos", "Ajuizados")
    
    var showAddAgreement by remember { mutableStateOf(false) }
    var showAddDelinquent by remember { mutableStateOf(false) }
    var showAddLawsuit by remember { mutableStateOf(false) }
    
    var agreementToEdit by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var delinquentToEdit by remember { mutableStateOf<DelinquentEntity?>(null) }
    var lawsuitToEdit by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var agreementToRenew by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var showHistoryForApt by remember { mutableStateOf<String?>(null) }
    var reportChoiceFor by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var showNotificationDetails by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var lawsuitForSuccess by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForDelete by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForEditWithPwd by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var statusFilter by remember { mutableStateOf("Todos") }
    var expandedFilter by remember { mutableStateOf(false) }
    
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val selected = agreements.find { it.agreement.id == selectedId }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedIds.size} selecionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
            }
            
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> {
                        val sortedDelinquents = delinquents.sortedBy { naturalSortApartments(it.delinquent.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedDelinquents) { item -> 
                                val isSelected = selectedIds.contains(item.delinquent.id)
                                DelinquentCard(
                                    item = item,
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.delinquent.id else selectedIds + item.delinquent.id
                                            } else {
                                                delinquentToEdit = item.delinquent
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.delinquent.id
                                            }
                                        }
                                    ),
                                    onEdit = { 
                                        if (selectedIds.isEmpty()) delinquentToEdit = item.delinquent 
                                    },
                                    onArchive = { scope.launch { dao.updateDelinquent(item.delinquent.copy(isArchived = true)) } },
                                    onAddProgress = { date, desc ->
                                        scope.launch { dao.insertDelinquentProgress(DelinquentProgressEntity(delinquentId = item.delinquent.id, apartment = item.delinquent.apartment, date = date, description = desc)) }
                                    },
                                    onUpdateProgress = { progId, date, desc ->
                                        scope.launch { dao.updateDelinquentProgress(DelinquentProgressEntity(id = progId, delinquentId = item.delinquent.id, apartment = item.delinquent.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId ->
                                        scope.launch { dao.deleteDelinquentProgress(progId) }
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        val sortedAgreements = agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedAgreements) { item -> 
                                val isSelected = selectedIds.contains(item.agreement.id)
                                AgreementCard(
                                    agreement = item,
                                    isCompact = isCompact,
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.agreement.id else selectedIds + item.agreement.id
                                            } else {
                                                agreementToEdit = item
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.agreement.id
                                            }
                                        }
                                    ),
                                    onRenew = { agreementToRenew = item },
                                    onViewHistory = { showHistoryForApt = item.agreement.apartment },
                                    onShare = { reportChoiceFor = item },
                                    onShowNotifs = { showNotificationDetails = item },
                                    onAjuizar = {
                                        lawsuitToEdit = LawsuitEntity(apartment = item.agreement.apartment, ownerName = item.agreement.ownerName, totalDebt = item.agreement.totalDebt, registrationDate = item.agreement.date, processNumber = "", forum = "")
                                        selectedTab = 2
                                    },
                                    onArchive = { scope.launch { dao.updateAgreement(item.agreement.copy(isArchived = true)) } },
                                    onDetails = { selectedId = item.agreement.id },
                                    onAddProgress = { date, desc ->
                                        scope.launch {
                                            dao.insertAgreementProgress(AgreementProgressEntity(agreementId = item.agreement.id, apartment = item.agreement.apartment, date = date, description = desc))

                                            // Sincronizar com Notificações
                                            val delinquent = dao.getDelinquentByApartment(item.agreement.apartment)
                                            if (delinquent != null) {
                                                dao.insertDelinquentProgress(DelinquentProgressEntity(delinquentId = delinquent.id, apartment = delinquent.apartment, date = date, description = desc))
                                                // Atualizar campos do card
                                                dao.updateDelinquent(delinquent.copy(
                                                    ownerName = item.agreement.ownerName,
                                                    totalDebt = item.agreement.totalDebt,
                                                    hasMadeAgreement = true
                                                ))
                                            }
                                        }
                                    },
                                    onUpdateProgress = { progId, date, desc ->
                                        scope.launch { dao.updateAgreementProgress(AgreementProgressEntity(id = progId, agreementId = item.agreement.id, apartment = item.agreement.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId ->
                                        scope.launch { dao.deleteAgreementProgress(progId) }
                                    }
                                )
                            }
                            if (sortedAgreements.isNotEmpty()) item { SummarySection(sortedAgreements) }
                        }
                    }
                    2 -> {
                        val uniqueStatuses = remember(lawsuits) { listOf("Todos") + lawsuits.map { it.lawsuit.status }.distinct().sorted() }
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedFilter,
                            onExpandedChange = { expandedFilter = !expandedFilter },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = statusFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filtrar por Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilter) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedFilter,
                                onDismissRequest = { expandedFilter = false }
                            ) {
                                uniqueStatuses.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            statusFilter = selectionOption
                                            expandedFilter = false
                                        }
                                    )
                                }
                            }
                        }

                        val filteredLawsuits = if (statusFilter == "Todos") lawsuits 
                                               else lawsuits.filter { it.lawsuit.status == statusFilter }
                        
                        val sortedLawsuits = filteredLawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedLawsuits) { item -> 
                                val isSelected = selectedIds.contains(item.lawsuit.id)
                                LawsuitCard(
                                    apartment = item.lawsuit.apartment,
                                    ownerName = item.lawsuit.ownerName,
                                    processNumber = item.lawsuit.processNumber,
                                    forum = item.lawsuit.forum,
                                    totalDebt = item.lawsuit.totalDebt,
                                    registrationDate = item.lawsuit.registrationDate,
                                    status = item.lawsuit.status,
                                    successValue = item.lawsuit.successValue,
                                    isFinished = item.lawsuit.isFinished,
                                    progressUpdates = item.progress,
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.lawsuit.id else selectedIds + item.lawsuit.id
                                            } else {
                                                if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                                else lawsuitToEdit = item.lawsuit
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.lawsuit.id
                                            }
                                        }
                                    ),
                                    onEdit = {
                                        if (selectedIds.isEmpty()) {
                                            if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                            else lawsuitToEdit = item.lawsuit
                                        }
                                    },
                                    onShare = { shareLawsuitReport(context, item.lawsuit) },
                                    onSuccess = { lawsuitForSuccess = item.lawsuit },
                                    onAddProgress = { date, desc ->
                                        scope.launch { dao.insertLawsuitProgress(LawsuitProgressEntity(lawsuitId = item.lawsuit.id, apartment = item.lawsuit.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId ->
                                        scope.launch { dao.deleteLawsuitProgress(progId) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(onClick = { 
            when (selectedTab) {
                0 -> showAddDelinquent = true
                1 -> showAddAgreement = true
                2 -> showAddLawsuit = true
            }
        }, Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Filled.Add, null) }
        
        if (showAddAgreement || agreementToEdit != null) RegisterAgreementDialog(
            editingAgreement = agreementToEdit, 
            onDismiss = { showAddAgreement = false; agreementToEdit = null }, 
            onConfirm = { agg, insts -> 
                val isNewAgreement = agreements.none { it.agreement.id == agg.id }
                scope.launch { 
                    dao.insertAgreement(agg)
                    dao.insertInstallments(insts)
                    FirestoreSyncManager.syncAgreement(agg)
                    
                    // Sincronizar com Notificações
                    val delinquent = dao.getDelinquentByApartment(agg.apartment)
                    if (delinquent != null) {
                        dao.updateDelinquent(delinquent.copy(
                            ownerName = agg.ownerName,
                            totalDebt = agg.totalDebt,
                            hasMadeAgreement = true
                        ))
                    }

                    if (isNewAgreement) {
                        dao.insertHistory(DelinquencyHistoryEntity(
                            apartment = agg.apartment,
                            ownerName = agg.ownerName,
                            eventType = "Acordo",
                            description = "Acordo",
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            value = agg.totalDebt
                        ))
                    }
                }
                showAddAgreement = false; agreementToEdit = null 
            },
            onAjuizar = { agg ->
                lawsuitToEdit = LawsuitEntity(apartment = agg.apartment, ownerName = agg.ownerName, totalDebt = agg.totalDebt, registrationDate = agg.date, processNumber = "", forum = "")
                selectedTab = 2
                showAddAgreement = false; agreementToEdit = null
            }
        )
        if (showAddDelinquent || delinquentToEdit != null) RegisterDelinquentDialog(editingDelinquent = delinquentToEdit, onDismiss = { showAddDelinquent = false; delinquentToEdit = null }, onConfirm = { del -> 
            val isNew = delinquentToEdit == null
            
            scope.launch { 
                if (isNew) {
                    dao.insertDelinquent(del)
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = del.apartment,
                        ownerName = del.ownerName,
                        eventType = "Notificação",
                        description = "Notificação",
                        date = del.registrationDate,
                        value = del.totalDebt
                    ))
                } else {
                    dao.updateDelinquent(del)
                }
                FirestoreSyncManager.syncDelinquent(del)
            }
            if (del.hasMadeAgreement) {
                val prefill = AgreementEntity(System.currentTimeMillis(), del.apartment, del.ownerName, del.totalDebt, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), 1, del.totalDebt, false, null, false, del.notification1Date, del.notification2Date, del.notification3Date)
                agreementToEdit = AgreementWithInstallments(prefill, emptyList())
                selectedTab = 1
            }
            showAddDelinquent = false; delinquentToEdit = null 
        })
        if (showAddLawsuit || lawsuitToEdit != null) RegisterLawsuitDialog(lawsuitToEdit, dao, { showAddLawsuit = false; lawsuitToEdit = null }, { lawsuit -> 
            val isNew = lawsuitToEdit == null || lawsuitToEdit!!.processNumber.isEmpty()
            scope.launch { 
                dao.insertLawsuit(lawsuit)
                FirestoreSyncManager.syncLawsuit(lawsuit)
                if (isNew) {
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = lawsuit.apartment,
                        ownerName = lawsuit.ownerName,
                        eventType = "Processo",
                        description = "Processo",
                        date = lawsuit.registrationDate,
                        value = lawsuit.totalDebt
                    ))
                }
                // Marcamos o acordo como ajuizado localmente na lista e no DB
                agreements.find { it.agreement.apartment == lawsuit.apartment }?.let {
                    dao.updateAgreement(it.agreement.copy(isLawsuit = true))
                }
            }
            showAddLawsuit = false; lawsuitToEdit = null 
        })
        
        if (agreementToRenew != null) {
            val agreementToCapture = agreementToRenew!!
            RegisterAgreementDialog(title = "Renovar Acordo", editingAgreement = agreementToCapture, isRenewal = true, onDismiss = { agreementToRenew = null }, onConfirm = { agg, insts -> 
                scope.launch { 
                    dao.updateAgreement(agreementToCapture.agreement.copy(isArchived = true))
                    dao.insertAgreement(agg.copy(originalAgreementId = agreementToCapture.agreement.id))
                    dao.insertInstallments(insts)
                    
                    // Sincronizar com Notificações
                    val delinquent = dao.getDelinquentByApartment(agg.apartment)
                    if (delinquent != null) {
                        dao.updateDelinquent(delinquent.copy(
                            ownerName = agg.ownerName,
                            totalDebt = agg.totalDebt,
                            hasMadeAgreement = true
                        ))
                    }
    
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = agg.apartment,
                        ownerName = agg.ownerName,
                        eventType = "Acordo",
                        description = "Acordo",
                        date = agg.date,
                        value = agg.totalDebt
                    ))
                }
                agreementToRenew = null 
            })
        }
        if (selected != null) InstallmentsDetailsDialog(selected, { selectedId = null }, { inst -> scope.launch { dao.updateInstallment(inst) } })
        if (showHistoryForApt != null) { val history by dao.getAgreementHistory(showHistoryForApt!!).collectAsState(initial = emptyList()); HistoryDialog(showHistoryForApt!!, history, onDismiss = { showHistoryForApt = null }) }
        if (reportChoiceFor != null) { ReportTypeDialog({ reportChoiceFor = null }, { det -> shareAgreementReport(context, reportChoiceFor!!, det); reportChoiceFor = null }) }
        if (showNotificationDetails != null) NotificationHistoryDialog(showNotificationDetails!!, onDismiss = { showNotificationDetails = null })
        if (lawsuitForSuccess != null) LawsuitSuccessDialog(lawsuitForSuccess!!, { lawsuitForSuccess = null }, { 
            val updated = it.copy(isFinished = true)
            scope.launch { 
                dao.updateLawsuit(updated)
                dao.insertHistory(DelinquencyHistoryEntity(
                    apartment = updated.apartment,
                    ownerName = updated.ownerName,
                    eventType = "Processo",
                    description = "Processo finalizado com êxito. Valor recuperado: R$ ${formatCurrency(updated.successValue ?: 0.0)}",
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    value = updated.successValue ?: 0.0
                ))
            }
            lawsuitForSuccess = null 
        })
        if (lawsuitForDelete != null) {
            val lawsuitToCapture = lawsuitForDelete!!
            PasswordUndoDialog({ lawsuitForDelete = null }, { 
                scope.launch { 
                    dao.deleteLawsuit(lawsuitToCapture.id)
                    agreements.find { it.agreement.apartment == lawsuitToCapture.apartment }?.let {
                        dao.updateAgreement(it.agreement.copy(isLawsuit = false))
                    }
                }
                lawsuitForDelete = null 
            })
        }
        if (lawsuitForEditWithPwd != null) PasswordUndoDialog({ lawsuitForEditWithPwd = null }, { 
            lawsuitToEdit = lawsuitForEditWithPwd
            lawsuitForEditWithPwd = null 
        })

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir os ${selectedIds.size} itens selecionados?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                selectedIds.forEach { id ->
                                    when (selectedTab) {
                                        0 -> dao.deleteDelinquent(id)
                                        1 -> dao.deleteAgreement(id)
                                        2 -> {
                                            val lawsuit = lawsuits.find { it.lawsuit.id == id }?.lawsuit
                                            dao.deleteLawsuit(id)
                                            if (lawsuit != null) {
                                                agreements.find { it.agreement.apartment == lawsuit.apartment }?.let { found ->
                                                    dao.updateAgreement(found.agreement.copy(isLawsuit = false))
                                                }
                                            }
                                        }
                                    }
                                }
                                selectedIds = emptySet()
                                showDeleteConfirm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun NotificationHistoryDialog(agreement: AgreementWithInstallments, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Notificações Prévias") },
        text = {
            Column {
                Text("Unidade: ${agreement.agreement.apartment}")
                Spacer(Modifier.height(8.dp))
                NotificationLine("1ª Notificação", agreement.agreement.n1Date)
                NotificationLine("2ª Notificação", agreement.agreement.n2Date)
                NotificationLine("3ª Notificação", agreement.agreement.n3Date)
                if (agreement.agreement.n1Date == null && agreement.agreement.n2Date == null && agreement.agreement.n3Date == null) {
                    Text("Nenhum histórico de notificação encontrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun SummarySection(active: List<AgreementWithInstallments>) {
    val totalAgreed = active.sumOf { it.agreement.totalDebt }; val totalPending = active.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    Card(Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text("Resumo Financeiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Total Acordado:"); Text("R$ ${formatCurrency(totalAgreed)}", fontWeight = FontWeight.Bold) }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Pendente:"); Text("R$ ${formatCurrency(totalPending)}", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63)) }
        }
    }
}

@Composable
fun AgreementCard(
    agreement: AgreementWithInstallments, 
    isCompact: Boolean = true,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onRenew: () -> Unit, 
    onViewHistory: () -> Unit, 
    onShare: () -> Unit, 
    onShowNotifs: () -> Unit, 
    onAjuizar: () -> Unit, 
    onArchive: () -> Unit, 
    onDetails: () -> Unit = {},
    onAddProgress: (String, String) -> Unit,
    onUpdateProgress: (Long, String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(!isCompact) }
    
    LaunchedEffect(isCompact) {
        expanded = !isCompact
    }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var progressToEdit by remember { mutableStateOf<AgreementProgressEntity?>(null) }
    val firstUnpaid = agreement.installments.sortedBy { it.number }.firstOrNull { !it.isPaid }
    val delay = firstUnpaid?.let { calculateDaysDelay(it.dueDate) } ?: 0
    val paidCount = agreement.installments.count { it.isPaid }
    val totalCount = agreement.agreement.installmentsCount
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f

    val isCondo = agreement.agreement.apartment == "CONDOMÍNIO"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Unidade e Status
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        agreement.agreement.ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: ${agreement.agreement.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (agreement.agreement.isLawsuit) {
                        Spacer(Modifier.height(4.dp))
                        Surface(color = Color.Red.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Gavel, null, Modifier.size(12.dp), tint = Color.Red)
                                Spacer(Modifier.width(4.dp))
                                Text("AJUIZADO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Red)
                            }
                        }
                    }
                }
                
                Surface(
                    color = when {
                        delay > 0 -> Color(0xFFFFCDD2).copy(alpha = 0.5f)
                        else -> Color(0xFFC8E6C9).copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (delay <= 0) "EM DIA" else "ATRASADO ($delay d)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (delay <= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Acordo firmado em ${agreement.agreement.date}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Dívida Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${formatCurrency(agreement.agreement.totalDebt)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Red)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Restante", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${formatCurrency(remaining)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Pagamento: $paidCount/$totalCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (progress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos do Acordo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (agreement.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    agreement.progress.sortedByDescending { it.id }.forEach { progress ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progress.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progress.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { progressToEdit = progress }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, "Editar Andamento", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { onDeleteProgress(progress.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (progress != agreement.progress.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botões de Ação
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Row {
                    IconButton({ onShare() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    IconButton({ onViewHistory() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton({ onShowNotifs() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                    IconButton({ onDetails() }, modifier = Modifier.size(36.dp)) { Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    if (!agreement.agreement.isLawsuit) {
                        IconButton({ onAjuizar() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Gavel, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                    }
                    IconButton({ onRenew() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Autorenew, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = { onArchive() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Archive, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }

    if (showAddProgressDialog) {
        AddAgreementProgressDialog(
            apartment = agreement.agreement.apartment,
            ownerName = agreement.agreement.ownerName,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }
}

// --- Inadimplentes Screen ---

@Composable
fun DelinquentCard(
    item: DelinquentWithProgress, 
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onAddProgress: (String, String) -> Unit,
    onUpdateProgress: (Long, String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var progressToEdit by remember { mutableStateOf<DelinquentProgressEntity?>(null) }

    val isCondo = item.delinquent.apartment == "CONDOMÍNIO"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.delinquent.ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else Color(0xFFE1BEE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: ${item.delinquent.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else Color(0xFF4A148C)
                        )
                    }
                }
                
                if (item.delinquent.hasMadeAgreement) {
                    Surface(
                        color = Color(0xFFC8E6C9).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "ACORDO FEITO",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val regDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.delinquent.id))
            Text("Inscrito em $regDateFormatted", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Ano da Dívida: ${item.delinquent.registrationDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.Red.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.1f))
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Dívida Atual", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                        Text("R$ ${formatCurrency(item.delinquent.totalDebt)}", color = Color.Red, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            Text("Fluxo de Notificações", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.spacedBy(8.dp)) {
                DelinquentNotificationBadge("1ª Notificação", item.delinquent.notification1Date != null)
                DelinquentNotificationBadge("2ª Notificação", item.delinquent.notification2Date != null)
                DelinquentNotificationBadge("3ª Notificação", item.delinquent.notification3Date != null)
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos da Notificação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (item.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    item.progress.sortedByDescending { it.id }.forEach { progress ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progress.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progress.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { progressToEdit = progress }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, "Editar Andamento", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { onDeleteProgress(progress.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (progress != item.progress.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Archive, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
            }
        }
    }

    if (showAddProgressDialog) {
        AddAgreementProgressDialog(
            apartment = item.delinquent.apartment,
            ownerName = item.delinquent.ownerName,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }

    if (progressToEdit != null) {
        EditProgressDialog(
            initialDate = progressToEdit!!.date,
            initialDesc = progressToEdit!!.description,
            onDismiss = { progressToEdit = null },
            onConfirm = { date, desc ->
                onUpdateProgress(progressToEdit!!.id, date, desc)
                progressToEdit = null
            }
        )
    }
}

@Composable
fun DelinquentNotificationBadge(label: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                Modifier.size(14.dp),
                tint = if (active) Color(0xFF2E7D32) else Color.Gray
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) Color(0xFF2E7D32) else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArchivedNotificationsDialog(archived: List<DelinquentWithProgress>, onDelete: (DelinquentEntity) -> Unit, onUnarchive: (DelinquentEntity) -> Unit, onDismiss: () -> Unit) {
    val sortedArchived = archived.sortedBy { naturalSortApartments(it.delinquent.apartment) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Notificações Arquivadas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (sortedArchived.isEmpty()) {
                    Text("Nenhuma notificação arquivada.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(sortedArchived) { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(item.delinquent.apartment, fontWeight = FontWeight.Bold)
                                    Row {
                                        IconButton({ onUnarchive(item.delinquent) }, Modifier.size(32.dp)) { Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary) }
                                        IconButton({ onDelete(item.delinquent) }, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                    }
                                }
                                Text(item.delinquent.ownerName, style = MaterialTheme.typography.bodySmall)
                                Text("Dívida: R$ ${formatCurrency(item.delinquent.totalDebt)}", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun NotificationLine(label: String, date: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (date != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, Modifier.size(14.dp), tint = if (date != null) Color(0xFF4CAF50) else Color.Gray)
        Spacer(Modifier.width(4.dp)); Text("$label: ${date ?: "Pendente"}", style = MaterialTheme.typography.labelSmall, color = if (date != null) Color.Unspecified else Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDelinquentDialog(editingDelinquent: DelinquentEntity? = null, onDismiss: () -> Unit, onConfirm: (DelinquentEntity) -> Unit) {
    var apto by remember { mutableStateOf(editingDelinquent?.apartment ?: "") }
    var nome by remember { mutableStateOf(editingDelinquent?.ownerName ?: "") }
    var divDigits by remember { mutableStateOf(editingDelinquent?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    
    val initialYears = editingDelinquent?.registrationDate?.split(Regex("[,/\\s]+"))?.filter { it.isNotBlank() } 
        ?: listOf(Calendar.getInstance().get(Calendar.YEAR).toString())
    val debtYearsList = remember { mutableStateListOf(*initialYears.toTypedArray()) }

    var hasMadeAgreement by remember { mutableStateOf(editingDelinquent?.hasMadeAgreement ?: false) }
    var n1Date by remember { mutableStateOf(editingDelinquent?.notification1Date ?: "") }
    var n2Date by remember { mutableStateOf(editingDelinquent?.notification2Date ?: "") }
    var n3Date by remember { mutableStateOf(editingDelinquent?.notification3Date ?: "") }
    var activePickerField by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingDelinquent != null) "Editar Unidade" else "Cadastrar para Notificação") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 500.dp)) {
                item { OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                item { OutlinedTextField(nome, { nome = it }, label = { Text("Nome do Proprietário") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                item { OutlinedTextField(divDigits, { if (it.length <= 12) divDigits = it.filter { char -> char.isDigit() } }, label = { Text("Dívida Total") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                
                item {
                    Text("Anos da Dívida:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                items(debtYearsList.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = debtYearsList[index],
                            onValueChange = { debtYearsList[index] = it },
                            label = { Text("Ano ${index + 1}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (debtYearsList.size > 1) {
                            IconButton(onClick = { debtYearsList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, "Remover Ano", tint = Color.Red)
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { debtYearsList.add(Calendar.getInstance().get(Calendar.YEAR).toString()) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Outro Ano da Dívida (+)")
                    }
                }

                item { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(hasMadeAgreement, { hasMadeAgreement = it }); Text("Fez acordo (Acordo Feito)") } }
                item { Text("Datas de Notificação:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                item { NotificationDateField("1ª Notificação", n1Date, { n1Date = "" }, { activePickerField = 1 }) }
                item { NotificationDateField("2ª Notificação", n2Date, { n2Date = "" }, { activePickerField = 2 }) }
                item { NotificationDateField("3ª Notificação", n3Date, { n3Date = "" }, { activePickerField = 3 }) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val combinedYears = debtYearsList.filter { it.isNotBlank() }.joinToString(", ")
                onConfirm(
                    DelinquentEntity(
                        id = editingDelinquent?.id ?: System.currentTimeMillis(),
                        apartment = apto,
                        ownerName = nome,
                        totalDebt = total,
                        registrationDate = if (combinedYears.isNotBlank()) combinedYears else Calendar.getInstance().get(Calendar.YEAR).toString(),
                        notification1Date = n1Date.ifBlank { null },
                        notification2Date = n2Date.ifBlank { null },
                        notification3Date = n3Date.ifBlank { null },
                        hasMadeAgreement = hasMadeAgreement,
                        isArchived = editingDelinquent?.isArchived ?: false
                    )
                )
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onDismiss) {
                Text("Cancelar")
            }
        }
    )
    if (activePickerField != null) {
        val state = rememberDatePickerState(initialSelectedDateMillis = try { 
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(
                when (activePickerField) { 1 -> n1Date; 2 -> n2Date; 3 -> n3Date; else -> "" }
            )?.time 
        } catch(_: Exception) { null })
        DatePickerDialog({ activePickerField = null }, { 
            TextButton({ 
                state.selectedDateMillis?.let { 
                    val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
                    when (activePickerField) { 1 -> n1Date = f; 2 -> n2Date = f; 3 -> n3Date = f } 
                }
                activePickerField = null 
            }) { Text("OK") } 
        }) { DatePicker(state) }
    }
}

@Composable
fun NotificationDateField(label: String, value: String, onClear: () -> Unit, onPick: () -> Unit) {
    OutlinedTextField(value = value, onValueChange = {}, label = { Text(label) }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { onPick() }, trailingIcon = { Row { if (value.isNotBlank()) IconButton(onClear) { Icon(Icons.Default.Clear, null) } ; IconButton(onPick) { Icon(Icons.Default.CalendarToday, null) } } })
}

// --- Ajuizados Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawsuitsManagementScreen(lawsuits: List<LawsuitWithProgress>, agreements: List<AgreementWithInstallments>, dao: AppDao, scope: CoroutineScope) {
    var showAddDialog by remember { mutableStateOf(false) }; var lawsuitToEdit by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForSuccess by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForDelete by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForEditWithPwd by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var statusFilter by remember { mutableStateOf("Todos") }
    var expandedFilter by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    val uniqueStatuses = remember(lawsuits) { listOf("Todos") + lawsuits.map { it.lawsuit.status }.distinct().sorted() }
    val filteredLawsuits = if (statusFilter == "Todos") lawsuits else lawsuits.filter { it.lawsuit.status == statusFilter }
    val sortedLawsuits = filteredLawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }
    
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expandedFilter,
                onExpandedChange = { expandedFilter = !expandedFilter },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = statusFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilter) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false }
                ) {
                    uniqueStatuses.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                statusFilter = selectionOption
                                expandedFilter = false
                            }
                        )
                    }
                }
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sortedLawsuits) { item -> 
                    SwipeToDeleteContainer(item, onDelete = {
                        if (item.lawsuit.isFinished) lawsuitForDelete = item.lawsuit
                        else {
                            scope.launch {
                                dao.deleteLawsuit(item.lawsuit.id)
                                // Limpa o status no acordo correspondente
                                agreements.find { it.agreement.apartment == item.lawsuit.apartment }?.let {
                                    dao.updateAgreement(it.agreement.copy(isLawsuit = false))
                                }
                            }
                        }
                    }) {
                        LawsuitCard(
                            apartment = item.lawsuit.apartment,
                            ownerName = item.lawsuit.ownerName,
                            processNumber = item.lawsuit.processNumber,
                            forum = item.lawsuit.forum,
                            totalDebt = item.lawsuit.totalDebt,
                            registrationDate = item.lawsuit.registrationDate,
                            status = item.lawsuit.status,
                            successValue = item.lawsuit.successValue,
                            isFinished = item.lawsuit.isFinished,
                            progressUpdates = item.progress,
                            onAddProgress = { date, desc ->
                                scope.launch { dao.insertLawsuitProgress(LawsuitProgressEntity(lawsuitId = item.lawsuit.id, date = date, description = desc)) }
                            },
                            onDeleteProgress = { progId ->
                                scope.launch { dao.deleteLawsuitProgress(progId) }
                            },
                            onEdit = {
                                if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                else lawsuitToEdit = item.lawsuit
                            },
                            onShare = { shareLawsuitReport(context, item.lawsuit) },
                            onSuccess = { lawsuitForSuccess = item.lawsuit }
                        )
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAddDialog = true }, Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Filled.Add, null) }
        
        if (showAddDialog || lawsuitToEdit != null) RegisterLawsuitDialog(lawsuitToEdit, dao, { showAddDialog = false; lawsuitToEdit = null }, { lawsuit -> scope.launch { dao.insertLawsuit(lawsuit) }; showAddDialog = false; lawsuitToEdit = null })
        
        if (lawsuitForSuccess != null) LawsuitSuccessDialog(lawsuitForSuccess!!, { lawsuitForSuccess = null }, { 
            val updated = it.copy(isFinished = true)
            scope.launch { 
                dao.updateLawsuit(updated)
                dao.insertHistory(DelinquencyHistoryEntity(
                    apartment = updated.apartment,
                    ownerName = updated.ownerName,
                    eventType = "Processo",
                    description = "Processo",
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    value = updated.successValue ?: 0.0
                ))
            }
            lawsuitForSuccess = null 
        })
        if (lawsuitForDelete != null) {
            val lawsuitToCapture = lawsuitForDelete!!
            PasswordUndoDialog({ lawsuitForDelete = null }, { 
                scope.launch { 
                    dao.deleteLawsuit(lawsuitToCapture.id)
                    agreements.find { it.agreement.apartment == lawsuitToCapture.apartment }?.let {
                        dao.updateAgreement(it.agreement.copy(isLawsuit = false))
                    }
                }
                lawsuitForDelete = null 
            })
        }
        if (lawsuitForEditWithPwd != null) PasswordUndoDialog({ lawsuitForEditWithPwd = null }, { 
            lawsuitToEdit = lawsuitForEditWithPwd
            lawsuitForEditWithPwd = null 
        })
    }
}

// --- Contracts Screen ---

fun isContractExpired(validity: String): Boolean {
    if (validity.isBlank()) return false
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
    return try {
        val date = sdf.parse(validity.trim())
        date != null && date.before(Date())
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(dao: AppDao, scope: CoroutineScope, currentUser: UserEntity) {
    val context = LocalContext.current
    val contracts by dao.getContracts().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var contractToEdit by remember { mutableStateOf<ContractEntity?>(null) }
    var contractToDelete by remember { mutableStateOf<ContractEntity?>(null) }
    
    val isSyndicOrAdmin = currentUser.role == "Síndico" || currentUser.role == "ADMIN"

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Contratos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Repositório de contratos (PDF, DOCX, TXT)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isSyndicOrAdmin) {
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (contracts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum contrato cadastrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(contracts) { contract ->
                    val expired = isContractExpired(contract.validity)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isSyndicOrAdmin) {
                                        contractToEdit = contract
                                    } else {
                                        downloadContractFile(context, contract.filePath, contract.fileName)
                                    }
                                },
                                onLongClick = {
                                    if (isSyndicOrAdmin) {
                                        contractToDelete = contract
                                    }
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (expired) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (expired) BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)) else null
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (expired) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = null,
                                                tint = if (expired) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(contract.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                                            if (expired) {
                                                Surface(
                                                    color = Color.Red,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("⚠️ VENCIDO", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text("📁 ${contract.fileName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (contract.validity.isNotBlank()) {
                                            Text("⏳ Validade: ${contract.validity}", style = MaterialTheme.typography.labelSmall, color = if (expired) Color.Red else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(thickness = 0.5.dp)
                            Spacer(Modifier.height(12.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Adicionado em: ${contract.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text("Por: ${contract.authorUsername}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (isSyndicOrAdmin) {
                                        IconButton(
                                            onClick = { contractToDelete = contract },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "Excluir Contrato", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Button(
                                        onClick = { downloadContractFile(context, contract.filePath, contract.fileName) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Baixar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContractDialog(
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onAdd = { title, fileName, filePath, validity, contentText ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val c = ContractEntity(
                            title = title,
                            fileName = fileName,
                            filePath = filePath,
                            validity = validity,
                            contentText = contentText,
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            authorUsername = currentUser.username
                        )
                        dao.insertContract(c)
                        FirestoreSyncManager.syncContract(c)
                    }
                }
                showAddDialog = false
            }
        )
    }

    if (contractToEdit != null) {
        val currentContract = contractToEdit!!
        EditContractDialog(
            contract = currentContract,
            onDismiss = { contractToEdit = null },
            onUpdate = { newTitle, newValidity, newPath, newName ->
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val oldPath = currentContract.filePath
                            if (newPath != null && newPath != oldPath) {
                                try {
                                    val oldFile = File(oldPath)
                                    if (oldFile.exists()) {
                                        oldFile.delete()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            dao.updateContract(
                                currentContract.copy(
                                    title = newTitle,
                                    validity = newValidity,
                                    fileName = newName ?: currentContract.fileName,
                                    filePath = newPath ?: currentContract.filePath
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val errorDetails = "${e.javaClass.simpleName}: ${e.message ?: e.toString()}"
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Erro ao atualizar contrato: $errorDetails", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                contractToEdit = null
            }
        )
    }

    if (contractToDelete != null) {
        val currentToDelete = contractToDelete!!
        AlertDialog(
            onDismissRequest = { contractToDelete = null },
            title = { Text("Excluir Contrato") },
            text = { Text("Deseja realmente excluir o contrato \"${currentToDelete.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val file = File(currentToDelete.filePath)
                                    if (file.exists()) file.delete()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                dao.deleteContract(currentToDelete.id)
                                FirestoreSyncManager.syncContract(currentToDelete, isDelete = true)
                            }
                        }
                        contractToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { contractToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EditContractDialog(contract: ContractEntity, onDismiss: () -> Unit, onUpdate: (String, String, String?, String?) -> Unit) {
    var title by remember { mutableStateOf(contract.title) }
    var validity by remember { mutableStateOf(contract.validity) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var fileContentText by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            selectedUri = uri
            val name = getFileName(context, uri)
            selectedFileName = name
            if (name.endsWith(".txt", ignoreCase = true)) {
                try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        fileContentText = reader.readText()
                    }
                } catch (_: Exception) {
                    fileContentText = ""
                }
            } else {
                fileContentText = "[Documento binário: $name]"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Contrato") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Contrato") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = validity,
                    onValueChange = { validity = it },
                    label = { Text("Validade do Contrato (ex: 31/12/2027)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { fileLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Substituir Arquivo no Dispositivo")
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedFileName ?: contract.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (selectedFileName != null) "Novo arquivo selecionado" else "Arquivo atual",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            try {
                                val (newPath, newName) = withContext(Dispatchers.IO) {
                                    var path: String? = null
                                    var name: String? = null
                                    if (selectedUri != null) {
                                        path = copyFileToInternalStorage(context, selectedUri!!)
                                        name = selectedFileName
                                    }
                                    Pair(path, name)
                                }
                                onUpdate(title, validity, newPath, newName)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Erro ao processar arquivo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddContractDialog(currentUser: UserEntity, onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var validity by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var fileContentText by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            selectedUri = uri
            selectedFileName = getFileName(context, uri)
            if (selectedFileName.endsWith(".txt", ignoreCase = true)) {
                try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        fileContentText = reader.readText()
                    }
                } catch (_: Exception) {
                    fileContentText = ""
                }
            } else {
                fileContentText = "[Documento binário: $selectedFileName]"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Contrato") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Contrato") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = validity,
                    onValueChange = { validity = it },
                    label = { Text("Validade do Contrato (ex: 31/12/2027 ou 12 meses)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { fileLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Procurar Documento no Dispositivo")
                }

                // Miniatura / Thumbnail preview
                if (selectedUri != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedFileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (selectedFileName.endsWith(".txt", ignoreCase = true)) "Arquivo de Texto (.txt)" else "Documento Anexado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            IconButton(onClick = { 
                                selectedUri = null
                                selectedFileName = ""
                                fileContentText = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedUri != null) {
                        scope.launch {
                            val savedPath = withContext(Dispatchers.IO) {
                                copyFileToInternalStorage(context, selectedUri!!)
                            }
                            if (savedPath != null) {
                                onAdd(title, selectedFileName, savedPath, validity, fileContentText)
                            }
                        }
                    }
                },
                enabled = title.isNotBlank() && selectedUri != null
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
@Composable
fun ContractViewerDialog(contract: ContractEntity, onDismiss: () -> Unit) {
    var isFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val dialogModifier = if (isFullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = dialogModifier.padding(if (isFullScreen) 0.dp else 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(contract.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("📁 ${contract.fileName} • ${contract.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Full Screen Toggle Button
                        TextButton(onClick = { isFullScreen = !isFullScreen }) {
                            Text(if (isFullScreen) "Normal" else "Tela Cheia")
                        }
                        // Share button
                        IconButton(onClick = {
                            val report = StringBuilder()
                            report.append("📄 CONTRATO: ${contract.title}\n")
                            report.append("📁 ARQUIVO: ${contract.fileName}\n")
                            report.append("==========================================\n\n")
                            if (contract.contentText.isNotBlank()) report.append(contract.contentText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report.toString())
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Contrato"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                        }
                        // Close button
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                HorizontalDivider()

                // Text Content (.txt viewer)
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SelectionContainer {
                            Text(
                                text = contract.contentText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                // Footer
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun AddAgreementProgressDialog(apartment: String, ownerName: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Unidade: $apartment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Proprietário: $ownerName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun LawsuitCard(
    apartment: String, 
    ownerName: String, 
    processNumber: String, 
    forum: String, 
    totalDebt: Double, 
    registrationDate: String, 
    status: String,
    successValue: Double?,
    isFinished: Boolean,
    progressUpdates: List<LawsuitProgressEntity> = emptyList(),
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onSuccess: () -> Unit,
    onAddProgress: (String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }

    val isCondo = apartment == "CONDOMÍNIO"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Unidade e Status
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else Color(0xFFFFCDD2).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: $apartment",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else Color(0xFFB71C1C)
                        )
                    }
                }
                
                Surface(
                    color = if (isFinished) Color(0xFFC8E6C9).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) { 
                    Text(
                        text = if (isFinished) "FINALIZADO" else status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isFinished) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                } 
            }
            
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Processo: $processNumber", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Fórum: $forum", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajuizado em: $registrationDate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Dívida Ajuizada: R$ ${formatCurrency(totalDebt)}", color = Color.Red, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            
            if (successValue != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("VALOR RECUPERADO", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("R$ ${formatCurrency(successValue)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos do Processo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (progressUpdates.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    progressUpdates.sortedByDescending { it.id }.forEach { progress ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progress.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progress.description, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onDeleteProgress(progress.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        if (progress != progressUpdates.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                if (!isFinished) {
                    IconButton(onSuccess, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Stars, "Êxito", tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp)) }
                }
                IconButton(onShare, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
        }
    }

    if (showAddProgressDialog) {
        AddLawsuitProgressDialog(
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }
}

@Composable
fun AddLawsuitProgressDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditProgressDialog(initialDate: String, initialDesc: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf(initialDesc) }
    var date by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun LawsuitSuccessDialog(lawsuit: LawsuitEntity, onDismiss: () -> Unit, onConfirm: (LawsuitEntity) -> Unit) {
    var valDigits by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, 
        title = { Text("Declarar Êxito") }, 
        text = {
            Column {
                Text("Informe o valor obtido no processo:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = valDigits, 
                    onValueChange = { if (it.length <= 12) valDigits = it.filter { char -> char.isDigit() } }, 
                    label = { Text("Valor do Êxito") }, 
                    prefix = { Text("R$ ") }, 
                    visualTransformation = CurrencyVisualTransformation(), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { 
            Button(onClick = { 
                val value = (valDigits.toDoubleOrNull() ?: 0.0) / 100
                onConfirm(lawsuit.copy(successValue = value)) 
            }) { Text("Confirmar") } 
        }, 
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterLawsuitDialog(editingLawsuit: LawsuitEntity? = null, dao: AppDao, onDismiss: () -> Unit, onConfirm: (LawsuitEntity) -> Unit) {
    var apto by remember { mutableStateOf(editingLawsuit?.apartment ?: "") }; var nome by remember { mutableStateOf(editingLawsuit?.ownerName ?: "") }
    var procNumDigits by remember { mutableStateOf(editingLawsuit?.processNumber?.replace(Regex("\\D"), "") ?: "") }
    var forum by remember { mutableStateOf(editingLawsuit?.forum ?: "") }
    var divDigits by remember { mutableStateOf(editingLawsuit?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); var date by remember { mutableStateOf(editingLawsuit?.registrationDate ?: sdf.format(Date())) }
    
    val statusOptionsDb by dao.getProcessStatuses().collectAsState(initial = emptyList())
    val statusOptions = remember(statusOptionsDb) {
        val list = statusOptionsDb.map { it.status }.toMutableStateList()
        if (list.isEmpty()) {
            list.addAll(listOf("Enviado para ajuizar", "Em andamento", "Exito", "Perda"))
        }
        list
    }
    // Garante que se estiver editando, o status original esteja na lista
    editingLawsuit?.status?.let { if (it.isNotBlank() && !statusOptions.contains(it)) statusOptions.add(it) }
    
    var status by remember { mutableStateOf(editingLawsuit?.status ?: if (statusOptions.isNotEmpty()) statusOptions[1] else "") }
    var showPicker by remember { mutableStateOf(false) }
    var showAddCustomStatus by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (editingLawsuit != null) "Editar Processo" else "Cadastrar Processo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }); OutlinedTextField(nome, { nome = it }, label = { Text("Proprietário") })
                OutlinedTextField(
                    value = procNumDigits,
                    onValueChange = { it -> if (it.length <= 20) procNumDigits = it.filter { it.isDigit() } },
                    label = { Text("Nº do Processo") },
                    placeholder = { Text("0000000-00.0000.0.00.0000") },
                    visualTransformation = ProcessNumberVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(forum, { forum = it }, label = { Text("Fórum / Vara") })
                OutlinedTextField(divDigits, { it -> if (it.length <= 12) divDigits = it.filter { it.isDigit() } }, label = { Text("Dívida Ajuizada") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                OutlinedTextField(date, {}, label = { Text("Data do Ajuizamento") }, readOnly = true, trailingIcon = { IconButton({ showPicker = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.clickable { showPicker = true })
                
                var expandedStatus by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status do Processo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        status = selectionOption
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showAddCustomStatus = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, "Adicionar Status", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { 
            Button(onClick = { 
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                // Aplica a formatação final para salvar no banco
                val formattedProc = StringBuilder()
                for (i in procNumDigits.indices) {
                    formattedProc.append(procNumDigits[i])
                    when (i) {
                        6 -> formattedProc.append("-")
                        8, 12, 13, 15 -> formattedProc.append(".")
                    }
                }
                onConfirm(LawsuitEntity(editingLawsuit?.id ?: System.currentTimeMillis(), apto, nome, formattedProc.toString(), forum, total, date, status)) 
            }) { Text("Salvar") } 
        }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )

    if (showAddCustomStatus) {
        var newStatus by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomStatus = false },
            title = { Text("Cadastrar Status") },
            text = {
                OutlinedTextField(
                    value = newStatus,
                    onValueChange = { newStatus = it },
                    label = { Text("Novo Status") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newStatus.isNotBlank()) {
                        statusOptions.add(newStatus)
                        status = newStatus
                    }
                    showAddCustomStatus = false
                }) { Text("Adicionar") }
            },
            dismissButton = { TextButton({ showAddCustomStatus = false }) { Text("Cancelar") } }
        )
    }

    if (showPicker) { 
        val state = rememberDatePickerState(initialSelectedDateMillis = try { 
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(date)?.time 
        } catch(_: Exception) { null })
        DatePickerDialog({ showPicker = false }, { 
            TextButton({ 
                state.selectedDateMillis?.let { 
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
                }
                showPicker = false 
            }) { Text("OK") } 
        }) { DatePicker(state) } 
    }
}

@Composable
fun ElevatorsScreen(dao: AppDao, scope: CoroutineScope, currentUser: UserEntity) {
    val context = LocalContext.current; val orders by dao.getServiceOrders().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }; var orderToEdit by remember { mutableStateOf<ServiceOrderWithInstallments?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }; var reportChoiceFor by remember { mutableStateOf<ServiceOrderWithInstallments?>(null) }
    val tabs = listOf("Social", "Serviço")
    
    var searchDesc by remember { mutableStateOf("") }
    var searchFloor by remember { mutableStateOf("") }

    // Date Filter State
    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }
    
    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    
    // Rolling 12 months for selector
    val monthOptions = remember {
        (0..11).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            Triple(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), monthLabels[cal.get(Calendar.MONTH)])
        }.reversed()
    }

    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedIds.size} selecionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            SecondaryTabRow(selectedTabIndex = selectedTab) { tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) } }
            Column(Modifier.padding(16.dp)) {
                // Barra de Busca
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchDesc,
                        onValueChange = { searchDesc = it },
                        label = { Text("Busca Descrição") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = searchFloor,
                        onValueChange = { searchFloor = it },
                        label = { Text("Andar") },
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                }
                
                Spacer(Modifier.height(16.dp))

                // Modern Month Selector
                val listState = rememberLazyListState()
                LaunchedEffect(Unit) {
                    if (monthOptions.isNotEmpty()) {
                        listState.scrollToItem(monthOptions.size - 1)
                    }
                }

                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(monthOptions) { (m, y, label) ->
                        val isSelected = m == selectedMonth && y == selectedYear
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                selectedMonth = m
                                selectedYear = y
                            },
                            label = { Text("$label/${y.toString().takeLast(2)}") },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val filtered = orders.filter { 
                        val osDate = try {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.order.date)
                        } catch (_: Exception) { null }
                        val osCal = Calendar.getInstance().apply { if (osDate != null) time = osDate }

                        it.order.type == tabs[selectedTab] && 
                        it.order.description.contains(searchDesc, ignoreCase = true) &&
                        it.order.floor.contains(searchFloor, ignoreCase = true) &&
                        (osDate != null && osCal.get(Calendar.MONTH) == selectedMonth && osCal.get(Calendar.YEAR) == selectedYear)
                    }
                    items(filtered) { item -> 
                        val isSelected = selectedIds.contains(item.order.id)
                        ServiceOrderCard(
                            item = item, 
                            isSelected = isSelected,
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                        selectedIds = if (isSelected) selectedIds - item.order.id else selectedIds + item.order.id
                                    } else {
                                        orderToEdit = item
                                    }
                                },
                                onLongClick = {
                                    if (currentUser.role == "ADMIN") {
                                        selectedIds = selectedIds + item.order.id
                                    }
                                }
                            ),
                            onEdit = { 
                                if (selectedIds.isEmpty()) orderToEdit = item 
                            }, 
                            onShare = { reportChoiceFor = item }
                        ) 
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showDialog = true }, Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Filled.Build, null) }
        
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir as ${selectedIds.size} ordens de serviço selecionadas?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                selectedIds.forEach { id ->
                                    dao.deleteServiceOrder(id)
                                }
                                selectedIds = emptySet()
                                showDeleteConfirm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                }
            )
        }
        if (showDialog || orderToEdit != null) RegisterServiceOrderDialog(tabs[selectedTab], orderToEdit, dao, { showDialog = false; orderToEdit = null }, { order, insts -> scope.launch { dao.insertServiceOrder(order); dao.insertServiceInstallments(insts); FirestoreSyncManager.syncServiceOrder(order) }; showDialog = false; orderToEdit = null })
        if (reportChoiceFor != null) { ReportTypeDialog({ reportChoiceFor = null }, { det -> shareServiceOrderReport(context, reportChoiceFor!!, det); reportChoiceFor = null }) }
    }
}

@Composable
fun ServiceOrderCard(
    item: ServiceOrderWithInstallments, 
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit, 
    onShare: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF7F7F7)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        item.order.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (item.order.type == "Social") Color(0xFFBBDEFB).copy(alpha = 0.5f) else Color(0xFFFFECB3).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (item.order.type == "Social") Icons.Default.Groups else Icons.Default.Build,
                                null,
                                Modifier.size(12.dp),
                                tint = if (item.order.type == "Social") Color(0xFF1976D2) else Color(0xFFFFA000)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                item.order.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.order.type == "Social") Color(0xFF1976D2) else Color(0xFFFFA000)
                            )
                        }
                    }
                }
                Text(item.order.date, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Elevator, null, Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text("Andar: ${item.order.floor}", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = Color.Gray)
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Investimento", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${formatCurrency(item.order.totalValue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                
                if (item.order.isInstallment) {
                    val paid = item.installments.count { it.isPaid }
                    val total = item.installments.size
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Parcelado: $paid/$total",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                IconButton(onShare, modifier = Modifier.size(36.dp)) { 
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) 
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterServiceOrderDialog(initialType: String, editingOrder: ServiceOrderWithInstallments? = null, dao: AppDao, onDismiss: () -> Unit, onConfirm: (ServiceOrderEntity, List<ServiceInstallmentEntity>) -> Unit) {
    val descriptionOptionsDb by dao.getServiceDescriptions().collectAsState(initial = emptyList())
    val floorsDb by dao.getFloors().collectAsState(initial = emptyList())
    
    val descriptionOptions = remember(descriptionOptionsDb) {
        val list = descriptionOptionsDb.map { it.description }.toMutableStateList()
        if (list.isEmpty()) {
            list.addAll(listOf("TROCA DE DICTADOR", "TROCA DE BOTÃO", "TROCA DE VENTILADOR"))
        }
        list
    }
    
    // Garante que se estiver editando, a descrição original esteja na lista
    editingOrder?.order?.description?.let { if (it.isNotBlank() && !descriptionOptions.contains(it)) descriptionOptions.add(it) }
    
    var description by remember { mutableStateOf(editingOrder?.order?.description ?: if (descriptionOptions.isNotEmpty()) descriptionOptions[0] else "") }
    var floor by remember { mutableStateOf(editingOrder?.order?.floor ?: "T") }
    var valDigits by remember { mutableStateOf(editingOrder?.order?.totalValue?.let { (it * 100).toLong().toString() } ?: "") }; var isInst by remember { mutableStateOf(editingOrder?.order?.isInstallment ?: false) }
    var type by remember { mutableStateOf(editingOrder?.order?.type ?: initialType) }; var count by remember { mutableStateOf(editingOrder?.installments?.size?.toString() ?: "1") }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); var date by remember { mutableStateOf(editingOrder?.order?.date ?: sdf.format(Date())) }
    var firstDate by remember { mutableStateOf(editingOrder?.installments?.firstOrNull()?.dueDate ?: sdf.format(Date())) }; var showOS by remember { mutableStateOf(false) }; var showInst by remember { mutableStateOf(false) }
    var expandedFloor by remember { mutableStateOf(false) }
    var expandedDesc by remember { mutableStateOf(false) }
    
    val floors = remember(floorsDb) {
        val list = floorsDb.map { it.floor }
        if (list.isEmpty()) listOf("4S", "3S", "2S", "1S", "T", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12") else list
    }
    
    var showAddCustomDesc by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (editingOrder != null) "Editar OS" else "Nova OS") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(type == "Social", { type = "Social" }); Text("Social"); Spacer(Modifier.width(16.dp)); RadioButton(type == "Serviço", { type = "Serviço" }); Text("Serviço") } }
                item { OutlinedTextField(date, {}, label = { Text("Data da OS") }, readOnly = true, trailingIcon = { IconButton({ showOS = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.fillMaxWidth().clickable { showOS = true }) }
                
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDesc,
                            onExpandedChange = { expandedDesc = !expandedDesc },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = description,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Descrição") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDesc) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDesc,
                                onDismissRequest = { expandedDesc = false }
                            ) {
                                descriptionOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            description = selectionOption
                                            expandedDesc = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showAddCustomDesc = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, "Adicionar Descrição", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedFloor,
                        onExpandedChange = { expandedFloor = !expandedFloor },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = floor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Andar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloor) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFloor,
                            onDismissRequest = { expandedFloor = false }
                        ) {
                            floors.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        floor = selectionOption
                                        expandedFloor = false
                                    }
                                )
                            }
                        }
                    }
                }

                item { OutlinedTextField(valDigits, { it -> if (it.length <= 12) valDigits = it.filter { it.isDigit() } }, label = { Text("Valor") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth()) }
                item { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(isInst, { isInst = it }); Text("Parcelado") } }
                if (isInst) { 
                    item { OutlinedTextField(count, { count = it }, label = { Text("Parcelas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(firstDate, {}, label = { Text("1ª Parcela") }, readOnly = true, trailingIcon = { IconButton({ showInst = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.fillMaxWidth().clickable { showInst = true }) }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (valDigits.toDoubleOrNull() ?: 0.0) / 100
                val id = editingOrder?.order?.id ?: System.currentTimeMillis()
                val order = ServiceOrderEntity(id, date, description, floor, isInst, total, type)
                val insts = mutableListOf<ServiceInstallmentEntity>()
                if (isInst) { 
                    val c = (count.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                    val dates = calculateMonthlyDates(firstDate, c)
                    val v = total / c
                    for (i in 1..c) insts.add(ServiceInstallmentEntity(id + i + System.nanoTime(), id, i, v, dates[i-1])) 
                }
                onConfirm(order, insts)
            }) { Text("Salvar") }
        }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )

    if (showAddCustomDesc) {
        var newDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomDesc = false },
            title = { Text("Cadastrar Descrição") },
            text = {
                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { newDesc = it.uppercase() },
                    label = { Text("Nova Descrição") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newDesc.isNotBlank()) {
                        descriptionOptions.add(newDesc)
                        description = newDesc
                    }
                    showAddCustomDesc = false
                }) { Text("Adicionar") }
            },
            dismissButton = { TextButton({ showAddCustomDesc = false }) { Text("Cancelar") } }
        )
    }

    if (showOS || showInst) { 
        val state = rememberDatePickerState(initialSelectedDateMillis = try { 
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(if (showOS) date else firstDate)?.time 
        } catch(_: Exception) { null })
        DatePickerDialog({ showOS = false; showInst = false }, { 
            TextButton({ 
                state.selectedDateMillis?.let { 
                    val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it))
                    if (showOS) date = f else firstDate = f 
                }
                showOS = false; showInst = false 
            }) { Text("OK") } 
        }) { DatePicker(state) } 
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAgreementDialog(title: String = "Configurar Acordo", editingAgreement: AgreementWithInstallments? = null, isRenewal: Boolean = false, onDismiss: () -> Unit, onConfirm: (AgreementEntity, List<InstallmentEntity>) -> Unit, onAjuizar: (AgreementEntity) -> Unit = {}) {
    var apto by remember { mutableStateOf(editingAgreement?.agreement?.apartment ?: "") }
    var divDigits by remember { mutableStateOf(editingAgreement?.agreement?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dataAcordo by remember { mutableStateOf(editingAgreement?.agreement?.date ?: sdf.format(Date())) }
    var qtd by remember { mutableStateOf(editingAgreement?.agreement?.installmentsCount?.toString() ?: "1") }; var showPicker by remember { mutableStateOf(false) }
    
    var showQuotaDialog by remember { mutableStateOf(false) }
    val initialQuotas = editingAgreement?.agreement?.quotaMonths?.split(Regex("[,;\\s]+"))?.filter { it.isNotBlank() } ?: emptyList()
    val quotaList = remember { mutableStateListOf<String>().apply { addAll(initialQuotas) } }

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = try { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(dataAcordo)?.time 
    } catch(_: Exception) { System.currentTimeMillis() })
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (editingAgreement != null && !isRenewal) "Editar Acordo" else title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }, enabled = !isRenewal && editingAgreement == null, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(divDigits, { it -> if (it.length <= 12) divDigits = it.filter { it.isDigit() } }, label = { Text("Dívida") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(dataAcordo, {}, label = { Text("Data da 1ª parcela") }, readOnly = true, trailingIcon = { IconButton({ showPicker = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.clickable { showPicker = true }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(qtd, { qtd = it }, label = { Text("Parcelas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                
                // Cotas Condominiais section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Cotas Condominiais", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showQuotaDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (quotaList.isEmpty()) "Selecionar Cotas Condominiais (+)" else "Cotas Selecionadas (${quotaList.size}) (+)")
                    }

                    if (quotaList.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quotaList.sorted()) { quota ->
                                InputChip(
                                    selected = true,
                                    onClick = { quotaList.remove(quota) },
                                    label = { Text(quota) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }
                }

                val totalCalc = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val countCalc = qtd.toIntOrNull() ?: 1
                val valParcela = if (countCalc > 0) totalCalc / countCalc else 0.0
                
                OutlinedTextField(
                    value = formatCurrency(valParcela),
                    onValueChange = {},
                    label = { Text("Valor das Parcelas") },
                    readOnly = true,
                    prefix = { Text("R$ ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (editingAgreement != null) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val id = editingAgreement.agreement.id
                            val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                            val count = qtd.toIntOrNull() ?: 1
                            val valParcelaCalc = total / count
                            val ownerName = editingAgreement.agreement.ownerName
                            val combinedQuotas = quotaList.filter { it.isNotBlank() }.joinToString(";")
                            onAjuizar(AgreementEntity(id, apto, ownerName, total, dataAcordo, count, valParcelaCalc, false, editingAgreement.agreement.originalAgreementId, true, editingAgreement.agreement.n1Date, editingAgreement.agreement.n2Date, editingAgreement.agreement.n3Date, combinedQuotas))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Gavel, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Encaminhar para Jurídico")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val count = (qtd.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                val totalBD = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP)
                val valParcelaBD = totalBD.divide(BigDecimal.valueOf(count.toLong()), 2, RoundingMode.DOWN)
                val dates = calculateMonthlyDates(dataAcordo, count)
                var acc = BigDecimal.ZERO
                val insts = mutableListOf<InstallmentEntity>()
                val id = if (isRenewal) System.currentTimeMillis() else (editingAgreement?.agreement?.id ?: System.currentTimeMillis())
                for (i in 1..count) { 
                    val vBD = if (i == count) totalBD.subtract(acc) else valParcelaBD
                    acc = acc.add(vBD)
                    insts.add(InstallmentEntity(id + i + System.nanoTime(), id, i, vBD.toDouble(), dates[i-1])) 
                }
                val ownerName = editingAgreement?.agreement?.ownerName ?: "Proprietário"
                val combinedQuotas = quotaList.filter { it.isNotBlank() }.joinToString(";")
                onConfirm(AgreementEntity(id, apto, ownerName, total, dataAcordo, count, valParcelaBD.toDouble(), false, if (isRenewal) editingAgreement?.agreement?.id else editingAgreement?.agreement?.originalAgreementId, editingAgreement?.agreement?.isLawsuit ?: false, editingAgreement?.agreement?.n1Date, editingAgreement?.agreement?.n2Date, editingAgreement?.agreement?.n3Date, combinedQuotas), insts)
            }, shape = RoundedCornerShape(12.dp)) { Text("Salvar") }
        }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
    if (showPicker) DatePickerDialog({ showPicker = false }, { 
        TextButton({ 
            pickerState.selectedDateMillis?.let { 
                dataAcordo = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
            }
            showPicker = false 
        }) { Text("OK") } 
    }) { DatePicker(pickerState) }

    if (showQuotaDialog) {
        QuotaSelectionDialog(quotaList) {
            showQuotaDialog = false
        }
    }
}

@Composable
fun QuotaSelectionDialog(
    selectedQuotas: MutableList<String>,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    val years = listOf("2024", "2025", "2026", "2027", "2028")
    val months = listOf(
        Pair("01", "Janeiro"),
        Pair("02", "Fevereiro"),
        Pair("03", "Março"),
        Pair("04", "Abril"),
        Pair("05", "Maio"),
        Pair("06", "Junho"),
        Pair("07", "Julho"),
        Pair("08", "Agosto"),
        Pair("09", "Setembro"),
        Pair("10", "Outubro"),
        Pair("11", "Novembro"),
        Pair("12", "Dezembro")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Cotas Condominiais") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escolha o ano e selecione os meses (ex: 06/2024):", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    years.forEach { yr ->
                        FilterChip(
                            selected = selectedYear == yr,
                            onClick = { selectedYear = yr },
                            label = { Text(yr) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                months.forEach { pair ->
                    val num = pair.first
                    val name = pair.second
                    val quotaKey = "$num/$selectedYear"
                    val isChecked = selectedQuotas.contains(quotaKey)
                    Surface(
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isChecked) selectedQuotas.remove(quotaKey)
                            else selectedQuotas.add(quotaKey)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$name ($num/$selectedYear)", fontWeight = FontWeight.Medium)
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedQuotas.contains(quotaKey)) selectedQuotas.add(quotaKey)
                                    } else {
                                        selectedQuotas.remove(quotaKey)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Concluir")
            }
        }
    )
}



@Composable
fun InstallmentsDetailsDialog(agreement: AgreementWithInstallments, onDismiss: () -> Unit, onInstallmentUpdate: (InstallmentEntity) -> Unit) {
    var editDateFor by remember { mutableStateOf<InstallmentEntity?>(null) }; var undoFor by remember { mutableStateOf<InstallmentEntity?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Parcelas - ${agreement.agreement.apartment}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nº", Modifier.weight(0.5f), fontWeight = FontWeight.Bold); Text("Vencimento", Modifier.weight(1.1f), fontWeight = FontWeight.Bold); Text("Valor", Modifier.weight(0.9f), fontWeight = FontWeight.Bold); Text("Ação", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(agreement.installments.sortedBy { it.number }) { inst ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${inst.number}/${agreement.agreement.installmentsCount}", Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.weight(1.1f), verticalAlignment = Alignment.CenterVertically) { Text(inst.dueDate, style = MaterialTheme.typography.bodySmall); if (!inst.isPaid) IconButton({ editDateFor = inst }, Modifier.size(24.dp)) { Icon(Icons.Default.EditCalendar, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) } }
                            Text("R$ ${formatCurrency(inst.value)}", Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall)
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (!inst.isPaid) Button({ onInstallmentUpdate(inst.copy(isPaid = true)) }, Modifier.height(28.dp), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Pagar", style = MaterialTheme.typography.labelSmall) }
                                else IconButton({ undoFor = inst }, Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Filled.Undo, null, tint = Color(0xFF4CAF50)) }
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
                Spacer(Modifier.height(16.dp)); Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
        if (editDateFor != null) {
            val state = rememberDatePickerState(initialSelectedDateMillis = try { 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(editDateFor!!.dueDate)?.time 
            } catch(_: Exception) { null })
            DatePickerDialog(onDismissRequest = { editDateFor = null }, confirmButton = { 
                TextButton({ 
                    state.selectedDateMillis?.let { 
                        val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it))
                        onInstallmentUpdate(editDateFor!!.copy(dueDate = f)) 
                    }
                    editDateFor = null 
                }) { Text("OK") } 
            }, dismissButton = { TextButton({ editDateFor = null }) { Text("Cancelar") } }) { DatePicker(state) }
        }
        if (undoFor != null) PasswordUndoDialog({ undoFor = null }, { onInstallmentUpdate(undoFor!!.copy(isPaid = false)); undoFor = null })
    }
}

@Composable
fun ArchivedAgreementsDialog(archived: List<AgreementWithInstallments>, onDelete: (AgreementWithInstallments) -> Unit, onUnarchive: (AgreementWithInstallments) -> Unit, onDismiss: () -> Unit) {
    val sortedArchived = archived.sortedBy { naturalSortApartments(it.agreement.apartment) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Acordos Arquivados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (sortedArchived.isEmpty()) {
                    Text("Nenhum acordo arquivado.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(sortedArchived) { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(item.agreement.apartment, fontWeight = FontWeight.Bold)
                                    Row {
                                        IconButton({ onUnarchive(item) }, Modifier.size(32.dp)) { Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary) }
                                        IconButton({ onDelete(item) }, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                    }
                                }
                                Text(item.agreement.ownerName, style = MaterialTheme.typography.bodySmall)
                                Text("Dívida Original: R$ ${formatCurrency(item.agreement.totalDebt)}", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                Text("Data: ${item.agreement.date}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun PasswordUndoDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var pwd by remember { mutableStateOf("") }; var isErr by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Desfazer Pagamento") }, text = { Column { Text("Digite a senha:"); OutlinedTextField(value = pwd, onValueChange = { pwd = it; isErr = false }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), isError = isErr); if (isErr) Text("Senha incorreta!", color = Color.Red, style = MaterialTheme.typography.labelSmall) } },
        confirmButton = { Button({ if (pwd == "123456") onConfirm() else isErr = true }) { Text("Confirmar") } }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } })
}

@Composable
fun HistoryDialog(apartment: String, history: List<AgreementWithInstallments>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Histórico: $apartment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (history.isEmpty()) Text("Nenhum acordo anterior encontrado.")
                LazyColumn(Modifier.heightIn(max = 400.dp)) { items(history) { old -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(modifier = Modifier.padding(12.dp)) { Text("Data: ${old.agreement.date}", fontWeight = FontWeight.Bold); Text("Dívida: R$ ${formatCurrency(old.agreement.totalDebt)}"); val paid = old.installments.count { it.isPaid }; Text("Pagamento: $paid de ${old.agreement.installmentsCount} pagas", style = MaterialTheme.typography.bodySmall) } } } }
                Spacer(Modifier.height(16.dp)); Button(onDismiss, Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun SettingsScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val isAdmin = currentUser.role == "ADMIN"
    val tabs = if (isAdmin) listOf("Backup", "Limpeza", "Tabelas", "Logs Notif.", "Usuários") else listOf("Backup", "Limpeza", "Tabelas", "Logs Notif.")
    
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }
        
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> BackupSection(dao, currentUser, scope, context)
                1 -> CleanupSection(dao, scope)
                2 -> TablesManagementSection(dao, scope)
                3 -> NotificationLogsSection(dao, scope)
                4 -> if (isAdmin) UserManagementScreen(dao, scope)
            }
        }
    }
}

@Composable
fun NotificationLogsSection(dao: AppDao, scope: CoroutineScope) {
    val logs by dao.getNotificationLogs().collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Log de Envio de Notificações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (logs.isNotEmpty()) {
                TextButton(onClick = { scope.launch { dao.clearNotificationLogs() } }) {
                    Text("Limpar Logs", color = Color.Red)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum log de notificação registrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (log.status.contains("Sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(log.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(log.message, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                color = if (log.status.contains("Sucesso")) Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    log.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.status.contains("Sucesso")) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupSection(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope, context: Context) {
    var showExportResult by remember { mutableStateOf<String?>(null) }
    var exportProgressMap by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var isExporting by remember { mutableStateOf(false) }
    var showProgressSection by remember { mutableStateOf(false) }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                showProgressSection = true
                val success = ExcelHelper.exportDatabase(context, dao, it) { tableName, progress ->
                    exportProgressMap = exportProgressMap + (tableName to progress)
                }
                showExportResult = if (success) "Exportação concluída com sucesso!" else "Falha na exportação."
                isExporting = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val success = ExcelHelper.importDatabase(context, dao, it)
                showExportResult = if (success) "Importação concluída com sucesso!" else "Falha na importação."
                isExporting = false
            }
        }
    }

    // Launchers específicos para Notificações
    val exportNotifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val success = ExcelHelper.exportNotificationsTable(context, dao, it)
                showExportResult = if (success) "Tabela de notificações exportada com sucesso!" else "Falha ao exportar."
                isExporting = false
            }
        }
    }

    val importNotifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                isExporting = true
                val result = ExcelHelper.importNotificationsTable(context, dao, it)
                showExportResult = result
                isExporting = false
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Como Importar / Exportar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "• Use 'Exportar' para gerar uma planilha Excel (.xlsx) com seus dados atuais.\n" +
                    "• Use 'Importar' para carregar dados de uma planilha salva anteriormente.\n" +
                    "• Importante: Mantenha a estrutura das colunas e os nomes das abas conforme o arquivo exportado.\n" +
                    "• Unidades são identificadas pelo número do 'Apto'. Ao importar, registros existentes serão atualizados.\n" +
                    "• Notificações: A coluna 'Andamentos' deve conter o histórico no formato 'DD/MM/AAAA: Descrição' separado por quebras de linha.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text("Backup e Dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = { exportLauncher.launch("CondSuites_Backup_${System.currentTimeMillis()}.xlsx") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            enabled = !isExporting
        ) {
            Icon(Icons.Default.Upload, null)
            Spacer(Modifier.width(8.dp))
            Text(if (isExporting) "Exportando..." else "Exportar Banco de Dados (Excel)")
        }
        
        if (showProgressSection) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showProgressSection = !showProgressSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Progresso da Exportação", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Icon(if (showProgressSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    
                    if (showProgressSection) {
                        Spacer(Modifier.height(8.dp))
                        exportProgressMap.forEach { (tableName, progress) ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(tableName, style = MaterialTheme.typography.labelSmall)
                                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                        if (isExporting) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = { importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            enabled = !isExporting
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Importar Banco de Dados (Excel)")
        }

        if (currentUser.role == "ADMIN") {
            Spacer(Modifier.height(24.dp))
            Text("Ações Específicas (Notificações)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        val fileName = "Notificacoes_${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}.xlsx"
                        exportNotifLauncher.launch(fileName) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = { 
                        importNotifLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar", style = MaterialTheme.typography.labelSmall)
                }
            }

            var showLogs by remember { mutableStateOf(false) }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showLogs = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, null)
                Spacer(Modifier.width(8.dp))
                Text("Consultar Logs de Ocorrências")
            }
            
            if (showLogs) {
                OccurrenceLogsDialog(dao) { showLogs = false }
            }
        }
    }

    if (showExportResult != null) {
        AlertDialog(
            onDismissRequest = { showExportResult = null }, 
            title = { Text("Resultado") }, 
            text = { 
                Box(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(showExportResult!!) 
                }
            }, 
            confirmButton = { Button({ showExportResult = null }) { Text("OK") } }
        )
    }
}

@Composable
fun CleanupSection(dao: AppDao, scope: CoroutineScope) {
    var tableToClear by remember { mutableStateOf<String?>(null) }
    
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Limpeza de Dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Atenção: Estas ações são irreversíveis.", style = MaterialTheme.typography.bodySmall, color = Color.Red)
        Spacer(Modifier.height(16.dp))

        ClearDataButton("Limpar Notificações", Icons.Default.Notifications) { tableToClear = "Notificações" }
        ClearDataButton("Limpar Acordos", Icons.Filled.Handshake) { tableToClear = "Acordos" }
        ClearDataButton("Limpar Processos Ajuizados", Icons.Filled.Gavel) { tableToClear = "Ajuizados" }
        ClearDataButton("Limpar Manutenções (Elevadores)", Icons.Filled.Handyman) { tableToClear = "Manutenções" }
        ClearDataButton("Limpar Histórico de Inadimplência", Icons.Default.History) { tableToClear = "Histórico" }
        ClearDataButton("Limpar Ocorrências", Icons.Default.Assignment) { tableToClear = "Ocorrências" }
        ClearDataButton("Limpar Contratos", Icons.Default.Description) { tableToClear = "Contratos" }
        ClearDataButton("Limpar Usuários (Exceto Admin)", Icons.Default.People) { tableToClear = "Usuários" }
        ClearDataButton("Limpar Tabelas Auxiliares", Icons.Default.Settings) { tableToClear = "Auxiliares" }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { tableToClear = "TUDO" },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Icon(Icons.Default.DeleteForever, null)
            Spacer(Modifier.width(12.dp))
            Text("LIMPAR TODA A BASE DE DADOS")
        }
    }

    if (tableToClear != null) {
        PasswordUndoDialog(
            onDismiss = { tableToClear = null },
            onConfirm = {
                scope.launch {
                    when (tableToClear) {
                        "Notificações" -> dao.clearNotifications()
                        "Acordos" -> dao.clearAgreements()
                        "Ajuizados" -> dao.clearLawsuits()
                        "Manutenções" -> dao.clearServiceOrders()
                        "Histórico" -> dao.clearHistory()
                        "Ocorrências" -> dao.clearOccurrences()
                        "Contratos" -> dao.clearContracts()
                        "Usuários" -> dao.clearUsers()
                        "Auxiliares" -> {
                            dao.clearFloors()
                            dao.clearServiceDescriptions()
                            dao.clearProcessStatuses()
                            dao.clearOccurrenceTypes()
                        }
                        "TUDO" -> {
                            dao.clearNotifications()
                            dao.clearAgreements()
                            dao.clearLawsuits()
                            dao.clearServiceOrders()
                            dao.clearHistory()
                            dao.clearOccurrences()
                            dao.clearContracts()
                            dao.clearUsers()
                            dao.clearFloors()
                            dao.clearServiceDescriptions()
                            dao.clearProcessStatuses()
                            dao.clearOccurrenceTypes()
                        }
                    }
                    tableToClear = null
                }
            }
        )
    }
}

@Composable
fun TablesManagementSection(dao: AppDao, scope: CoroutineScope) {
    val floors by dao.getFloors().collectAsState(initial = emptyList())
    val descriptions by dao.getServiceDescriptions().collectAsState(initial = emptyList())
    val statuses by dao.getProcessStatuses().collectAsState(initial = emptyList())
    val occurrenceTypes by dao.getOccurrenceTypes().collectAsState(initial = emptyList())
    
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Gerenciamento de Tabelas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Gerencie os registros das tabelas auxiliares do sistema.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        
        TableManagerItem("Andares", floors.map { it.floor }, { scope.launch { dao.insertFloor(FloorEntity(it)) } }, { scope.launch { dao.deleteFloor(it) } })
        Spacer(Modifier.height(16.dp))
        TableManagerItem("Descrições de Manutenção", descriptions.map { it.description }, { scope.launch { dao.insertServiceDescription(ServiceDescriptionEntity(it)) } }, { scope.launch { dao.deleteServiceDescription(it) } })
        Spacer(Modifier.height(16.dp))
        TableManagerItem("Status de Processos", statuses.map { it.status }, { scope.launch { dao.insertProcessStatus(ProcessStatusEntity(it)) } }, { scope.launch { dao.deleteProcessStatus(it) } })
        Spacer(Modifier.height(16.dp))
        TableManagerItem("Tipos de Ocorrência", occurrenceTypes.map { it.type }, { scope.launch { dao.insertOccurrenceType(OccurrenceTypeEntity(it)) } }, { scope.launch { dao.deleteOccurrenceType(it) } })
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun TableManagerItem(title: String, items: List<String>, onAdd: (String) -> Unit, onDelete: (String) -> Unit) {
    var newItem by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            items.size.toString(),
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newItem,
                        onValueChange = { newItem = it },
                        label = { Text("Novo item") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { if (newItem.isNotBlank()) { onAdd(newItem); newItem = "" } },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                if (items.isEmpty()) {
                    Text("Nenhum registro encontrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    Column(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)
                    ) {
                        items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                            if (index < items.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClearDataButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
fun DelinquencyHistoryScreen(dao: AppDao, scope: CoroutineScope, currentUser: UserEntity) {
    val history by dao.getDelinquencyHistory().collectAsState(initial = emptyList())
    val context = LocalContext.current
    var filterApt by remember { mutableStateOf("") }
    
    val filteredHistory = if (filterApt.isBlank()) history else history.filter { it.apartment.contains(filterApt, ignoreCase = true) }
    val groupedHistory = filteredHistory.groupBy { it.apartment }.toSortedMap(compareBy { naturalSortApartments(it) })

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = filterApt,
            onValueChange = { filterApt = it },
            label = { Text("Filtrar por Unidade") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (filterApt.isNotBlank()) IconButton({ filterApt = "" }) { Icon(Icons.Default.Clear, null) } }
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (groupedHistory.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum histórico encontrado.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                groupedHistory.forEach { (apt, items) ->
                    item {
                        UnitHistoryTable(apt, items, context, currentUser) { id ->
                            scope.launch { dao.deleteHistory(id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitHistoryTable(apartment: String, items: List<DelinquencyHistoryEntity>, context: Context, currentUser: UserEntity, onDelete: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically) {
                Text("Unidade: $apartment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                IconButton(onClick = { shareDelinquencyHistoryReport(context, items, apartment) }) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Data", Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Evento", Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Text("Valor", Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
                Spacer(Modifier.width(32.dp))
            }
            
            items.sortedByDescending { it.date }.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.date, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = when(item.eventType) {
                            "Notificação" -> Color(0xFFFFE0B2)
                            "Acordo" -> Color(0xFFC8E6C9)
                            "Processo" -> Color(0xFFFFCDD2)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(item.eventType, Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }

                    Text("R$ ${formatCurrency(item.value)}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Red, textAlign = TextAlign.End)

                    if (currentUser.role == "ADMIN") {
                        IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Spacer(Modifier.width(32.dp))
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDialog = true
                false // Don't dismiss yet, wait for dialog
            } else {
                false
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir este registro?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(item)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Excluir", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = Color.Gray.copy(alpha = 0.8f)
            Box(
                Modifier.fillMaxSize().background(color, RoundedCornerShape(8.dp)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White)
            }
        },
        content = { content(item) }
    )
}

object ExcelHelper {
    suspend fun exportDatabase(context: Context, dao: AppDao, uri: Uri, onProgress: (String, Float) -> Unit = { _, _ -> }): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val tableNames = listOf(
                    "Usuários", "Acordos", "Parcelas", "AndamentosAcordo", "Notificações", 
                    "AndamentosNotif", "Ajuizados", "Andamentos", "Manutenções", 
                    "ParcelasOS", "Ocorrências", "MensagensOcorrência", "AnexosOcorrência",
                    "VotosAnexos", "LogsOcorrência", "Histórico",
                    "ConfigAndares", "ConfigServiços", "ConfigStatusProc", "ConfigOcorrências"
                )
                
                fun updateProgress(name: String, p: Float, currentTableIdx: Int) {
                    val totalProgress = (currentTableIdx.toFloat() + p) / tableNames.size
                    onProgress(name, totalProgress)
                }

                // Sheet: Usuários
                updateProgress("Usuários", 0.1f, 0)
                val sheetUsers = workbook.createSheet("Usuários")
                val headerUsers = sheetUsers.createRow(0)
                listOf("ID", "Username", "Password", "Role").forEachIndexed { i, s -> headerUsers.createCell(i).setCellValue(s) }
                dao.getAllUsersFlow().first().forEachIndexed { i, item ->
                    val row = sheetUsers.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.username)
                    row.createCell(2).setCellValue(item.password)
                    row.createCell(3).setCellValue(item.role)
                }
                updateProgress("Usuários", 1.0f, 0)

                // Sheet: Acordos
                updateProgress("Acordos", 0.1f, 1)
                val sheetAgg = workbook.createSheet("Acordos")
                val headerAgg = sheetAgg.createRow(0)
                listOf("ID", "Apto", "Proprietário", "Dívida", "Data", "Parcelas", "Vl Parcela", "Ajuizado", "Arquivado", "OrigemID", "N1", "N2", "N3").forEachIndexed { i, s -> headerAgg.createCell(i).setCellValue(s) }
                dao.getAllAgreements().forEachIndexed { i, item ->
                    val row = sheetAgg.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.apartment)
                    row.createCell(2).setCellValue(item.ownerName)
                    row.createCell(3).setCellValue(item.totalDebt)
                    row.createCell(4).setCellValue(item.date)
                    row.createCell(5).setCellValue(item.installmentsCount.toDouble())
                    row.createCell(6).setCellValue(item.installmentValue)
                    row.createCell(7).setCellValue(if(item.isLawsuit) "Sim" else "Não")
                    row.createCell(8).setCellValue(if(item.isArchived) "Sim" else "Não")
                    row.createCell(9).setCellValue(item.originalAgreementId?.toDouble() ?: -1.0)
                    row.createCell(10).setCellValue(item.n1Date ?: "")
                    row.createCell(11).setCellValue(item.n2Date ?: "")
                    row.createCell(12).setCellValue(item.n3Date ?: "")
                }
                updateProgress("Acordos", 1.0f, 1)

                // Sheet: Parcelas
                updateProgress("Parcelas", 0.1f, 2)
                val sheetInst = workbook.createSheet("Parcelas")
                val headerInst = sheetInst.createRow(0)
                listOf("ID", "AgreementID", "Nº", "Valor", "Vencimento", "Pago").forEachIndexed { i, s -> headerInst.createCell(i).setCellValue(s) }
                dao.getAllInstallments().forEachIndexed { i, item ->
                    val row = sheetInst.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.agreementId.toDouble())
                    row.createCell(2).setCellValue(item.number.toDouble())
                    row.createCell(3).setCellValue(item.value)
                    row.createCell(4).setCellValue(item.dueDate)
                    row.createCell(5).setCellValue(if(item.isPaid) "Sim" else "Não")
                }
                updateProgress("Parcelas", 1.0f, 2)

                // Sheet: AndamentosAcordo
                updateProgress("AndamentosAcordo", 0.1f, 3)
                val sheetProgAgg = workbook.createSheet("AndamentosAcordo")
                val headerProgAgg = sheetProgAgg.createRow(0)
                listOf("ID", "AgreementID", "Data", "Descrição").forEachIndexed { i, s -> headerProgAgg.createCell(i).setCellValue(s) }
                dao.getAllAgreementProgress().forEachIndexed { i, item ->
                    val row = sheetProgAgg.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.agreementId.toDouble())
                    row.createCell(2).setCellValue(item.date)
                    row.createCell(3).setCellValue(item.description)
                }
                updateProgress("AndamentosAcordo", 1.0f, 3)

                // Sheet: Notificações
                updateProgress("Notificações", 0.1f, 4)
                val sheetNotif = workbook.createSheet("Notificações")
                val headerNotif = sheetNotif.createRow(0)
                listOf("ID", "Apto", "Proprietário", "Dívida Total", "Ano da Dívida", "Status Acordo", "Arquivado", "N1", "N2", "N3", "Andamentos").forEachIndexed { i, s -> headerNotif.createCell(i).setCellValue(s) }
                val allNotifProgress = dao.getAllDelinquentProgress().groupBy { it.delinquentId }
                dao.getAllDelinquents().forEachIndexed { i, item ->
                    val row = sheetNotif.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.apartment)
                    row.createCell(2).setCellValue(item.ownerName)
                    row.createCell(3).setCellValue(item.totalDebt)
                    row.createCell(4).setCellValue(item.registrationDate)
                    row.createCell(5).setCellValue(if(item.hasMadeAgreement) "Acordo Feito" else "Pendente")
                    row.createCell(6).setCellValue(if(item.isArchived) "Sim" else "Não")
                    row.createCell(7).setCellValue(item.notification1Date ?: "")
                    row.createCell(8).setCellValue(item.notification2Date ?: "")
                    row.createCell(9).setCellValue(item.notification3Date ?: "")
                    
                    val progressText = allNotifProgress[item.id]?.sortedBy { it.date }?.joinToString("\n") { 
                        "${it.date}: ${it.description}" 
                    } ?: ""
                    row.createCell(10).setCellValue(progressText)
                }
                updateProgress("Notificações", 1.0f, 4)

                // Sheet: AndamentosNotif
                updateProgress("AndamentosNotif", 0.1f, 5)
                val sheetProgNotif = workbook.createSheet("AndamentosNotif")
                val headerProgNotif = sheetProgNotif.createRow(0)
                listOf("ID", "DelinquentID", "Data", "Descrição").forEachIndexed { i, s -> headerProgNotif.createCell(i).setCellValue(s) }
                dao.getAllDelinquentProgress().forEachIndexed { i, item ->
                    val row = sheetProgNotif.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.delinquentId.toDouble())
                    row.createCell(2).setCellValue(item.date)
                    row.createCell(3).setCellValue(item.description)
                }
                updateProgress("AndamentosNotif", 1.0f, 5)

                // Sheet: Ajuizados
                updateProgress("Ajuizados", 0.1f, 6)
                val sheetLaw = workbook.createSheet("Ajuizados")
                val headerLaw = sheetLaw.createRow(0)
                listOf("ID", "Apto", "Proprietário", "Processo", "Fórum", "Dívida", "Data", "Status", "Exito", "Finalizado").forEachIndexed { i, s -> headerLaw.createCell(i).setCellValue(s) }
                dao.getAllLawsuits().forEachIndexed { i, item ->
                    val row = sheetLaw.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.apartment)
                    row.createCell(2).setCellValue(item.ownerName)
                    row.createCell(3).setCellValue(item.processNumber)
                    row.createCell(4).setCellValue(item.forum)
                    row.createCell(5).setCellValue(item.totalDebt)
                    row.createCell(6).setCellValue(item.registrationDate)
                    row.createCell(7).setCellValue(item.status)
                    row.createCell(8).setCellValue(item.successValue ?: 0.0)
                    row.createCell(9).setCellValue(if(item.isFinished) "Sim" else "Não")
                }
                updateProgress("Ajuizados", 1.0f, 6)

                // Sheet: Andamentos
                updateProgress("Andamentos", 0.1f, 7)
                val sheetProg = workbook.createSheet("Andamentos")
                val headerProg = sheetProg.createRow(0)
                listOf("ID", "LawsuitID", "Data", "Descrição").forEachIndexed { i, s -> headerProg.createCell(i).setCellValue(s) }
                dao.getAllLawsuitProgress().forEachIndexed { i, item ->
                    val row = sheetProg.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.lawsuitId.toDouble())
                    row.createCell(2).setCellValue(item.date)
                    row.createCell(3).setCellValue(item.description)
                }
                updateProgress("Andamentos", 1.0f, 7)

                // Sheet: Manutenções
                updateProgress("Manutenções", 0.1f, 8)
                val sheetOS = workbook.createSheet("Manutenções")
                val headerOS = sheetOS.createRow(0)
                listOf("ID", "Data", "Descrição", "Andar", "Parcelado", "Valor", "Tipo").forEachIndexed { i, s -> headerOS.createCell(i).setCellValue(s) }
                dao.getAllServiceOrders().forEachIndexed { i, item ->
                    val row = sheetOS.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.date)
                    row.createCell(2).setCellValue(item.description)
                    row.createCell(3).setCellValue(item.floor)
                    row.createCell(4).setCellValue(if(item.isInstallment) "Sim" else "Não")
                    row.createCell(5).setCellValue(item.totalValue)
                    row.createCell(6).setCellValue(item.type)
                }
                updateProgress("Manutenções", 1.0f, 8)

                // Sheet: ParcelasOS
                updateProgress("ParcelasOS", 0.1f, 9)
                val sheetOSInst = workbook.createSheet("ParcelasOS")
                val headerOSInst = sheetOSInst.createRow(0)
                listOf("ID", "OSID", "Nº", "Valor", "Vencimento", "Pago").forEachIndexed { i, s -> headerOSInst.createCell(i).setCellValue(s) }
                dao.getAllServiceInstallments().forEachIndexed { i, item ->
                    val row = sheetOSInst.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.serviceOrderId.toDouble())
                    row.createCell(2).setCellValue(item.number.toDouble())
                    row.createCell(3).setCellValue(item.value)
                    row.createCell(4).setCellValue(item.dueDate)
                    row.createCell(5).setCellValue(if(item.isPaid) "Sim" else "Não")
                }
                updateProgress("ParcelasOS", 1.0f, 9)

                // Sheet: Ocorrências
                updateProgress("Ocorrências", 0.1f, 10)
                val sheetOcc = workbook.createSheet("Ocorrências")
                val headerOcc = sheetOcc.createRow(0)
                listOf("ID", "Título", "Apto", "Status", "Criador", "Data", "Tipo Interno", "Tipo Ocorrência", "Urgente").forEachIndexed { i, s -> headerOcc.createCell(i).setCellValue(s) }
                dao.getAllOccurrences().first().forEachIndexed { i, item ->
                    val row = sheetOcc.createRow(i + 1)
                    row.createCell(0).setCellValue(item.occurrence.id.toDouble())
                    row.createCell(1).setCellValue(item.occurrence.title)
                    row.createCell(2).setCellValue(item.occurrence.apartment)
                    row.createCell(3).setCellValue(item.occurrence.status)
                    row.createCell(4).setCellValue(item.occurrence.createdByUsername)
                    row.createCell(5).setCellValue(item.occurrence.date)
                    row.createCell(6).setCellValue(item.occurrence.type)
                    row.createCell(7).setCellValue(item.occurrence.occurrenceType)
                    row.createCell(8).setCellValue(if(item.occurrence.isUrgent) "Sim" else "Não")
                }
                updateProgress("Ocorrências", 1.0f, 10)

                // Sheet: MensagensOcorrência
                updateProgress("MensagensOcorrência", 0.1f, 11)
                val sheetOccMsg = workbook.createSheet("MensagensOcorrência")
                val headerOccMsg = sheetOccMsg.createRow(0)
                listOf("ID", "OccurrenceID", "Sender", "Text", "Date", "Conselho", "Síndico", "Lida", "VotaçãoFechada", "Orcamento").forEachIndexed { i, s -> headerOccMsg.createCell(i).setCellValue(s) }
                dao.getAllOccurrences().first().flatMap { it.messages }.forEachIndexed { idxMsg, msgItemWithAtts ->
                    val msgItem = msgItemWithAtts.message
                    val row = sheetOccMsg.createRow(idxMsg + 1)
                    row.createCell(0).setCellValue(msgItem.id.toDouble())
                    row.createCell(1).setCellValue(msgItem.occurrenceId.toDouble())
                    row.createCell(2).setCellValue(msgItem.senderUsername)
                    row.createCell(3).setCellValue(msgItem.text)
                    row.createCell(4).setCellValue(msgItem.date)
                    row.createCell(5).setCellValue(if(msgItem.isCouncilOnly) "Sim" else "Não")
                    row.createCell(6).setCellValue(if(msgItem.isSindicoOnly) "Sim" else "Não")
                    row.createCell(7).setCellValue(if(msgItem.isRead) "Sim" else "Não")
                    row.createCell(8).setCellValue(if(msgItem.isVotingClosed) "Sim" else "Não")
                    row.createCell(9).setCellValue(if(msgItem.isBudget) "Sim" else "Não")
                }
                updateProgress("MensagensOcorrência", 1.0f, 11)

                // Sheet: AnexosOcorrência
                updateProgress("AnexosOcorrência", 0.1f, 12)
                val sheetAtt = workbook.createSheet("AnexosOcorrência")
                val headerAtt = sheetAtt.createRow(0)
                listOf("ID", "MessageID", "FileName", "FilePath").forEachIndexed { i, s -> headerAtt.createCell(i).setCellValue(s) }
                dao.getAllAttachments().forEachIndexed { i, item ->
                    val row = sheetAtt.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.messageId.toDouble())
                    row.createCell(2).setCellValue(item.fileName)
                    row.createCell(3).setCellValue(item.filePath)
                }
                updateProgress("AnexosOcorrência", 1.0f, 12)

                // Sheet: VotosAnexos
                updateProgress("VotosAnexos", 0.1f, 13)
                val sheetVotes = workbook.createSheet("VotosAnexos")
                val headerVotes = sheetVotes.createRow(0)
                listOf("ID", "AttachmentID", "Username").forEachIndexed { i, s -> headerVotes.createCell(i).setCellValue(s) }
                dao.getAllAttachmentVotes().forEachIndexed { i, item ->
                    val row = sheetVotes.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.attachmentId.toDouble())
                    row.createCell(2).setCellValue(item.username)
                }
                updateProgress("VotosAnexos", 1.0f, 13)

                // Sheet: LogsOcorrência
                updateProgress("LogsOcorrência", 0.1f, 14)
                val sheetOccLog = workbook.createSheet("LogsOcorrência")
                val headerOccLog = sheetOccLog.createRow(0)
                listOf("ID", "OccurrenceID", "User", "Action", "Timestamp").forEachIndexed { i, s -> headerOccLog.createCell(i).setCellValue(s) }
                dao.getAllOccurrenceLogs().first().forEachIndexed { i, item ->
                    val row = sheetOccLog.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.occurrenceId.toDouble())
                    row.createCell(2).setCellValue(item.username)
                    row.createCell(3).setCellValue(item.action)
                    row.createCell(4).setCellValue(item.timestamp.toDouble())
                }
                updateProgress("LogsOcorrência", 1.0f, 14)

                // Sheet: Histórico
                updateProgress("Histórico", 0.1f, 15)
                val sheetHist = workbook.createSheet("Historico")
                val headerHist = sheetHist.createRow(0)
                listOf("ID", "Apto", "Proprietário", "Evento", "Descrição", "Data", "Valor").forEachIndexed { i, s -> headerHist.createCell(i).setCellValue(s) }
                dao.getAllHistory().forEachIndexed { i, item ->
                    val row = sheetHist.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.apartment)
                    row.createCell(2).setCellValue(item.ownerName)
                    row.createCell(3).setCellValue(item.eventType)
                    row.createCell(4).setCellValue(item.description)
                    row.createCell(5).setCellValue(item.date)
                    row.createCell(6).setCellValue(item.value)
                }
                updateProgress("Histórico", 1.0f, 15)

                // Config Tables
                // Andares
                updateProgress("ConfigAndares", 0.5f, 16)
                val sheetFloors = workbook.createSheet("ConfigAndares")
                sheetFloors.createRow(0).createCell(0).setCellValue("Andar")
                dao.getFloors().first().forEachIndexed { i, item -> sheetFloors.createRow(i+1).createCell(0).setCellValue(item.floor) }
                updateProgress("ConfigAndares", 1.0f, 16)

                // Serviços
                updateProgress("ConfigServiços", 0.5f, 17)
                val sheetServ = workbook.createSheet("ConfigServiços")
                sheetServ.createRow(0).createCell(0).setCellValue("Descrição")
                dao.getServiceDescriptions().first().forEachIndexed { i, item -> sheetServ.createRow(i+1).createCell(0).setCellValue(item.description) }
                updateProgress("ConfigServiços", 1.0f, 17)

                // Status Processo
                updateProgress("ConfigStatusProc", 0.5f, 18)
                val sheetStat = workbook.createSheet("ConfigStatusProc")
                sheetStat.createRow(0).createCell(0).setCellValue("Status")
                dao.getProcessStatuses().first().forEachIndexed { i, item -> sheetStat.createRow(i+1).createCell(0).setCellValue(item.status) }
                updateProgress("ConfigStatusProc", 1.0f, 18)

                // Tipos Ocorrência
                updateProgress("ConfigOcorrências", 0.5f, 19)
                val sheetOccT = workbook.createSheet("ConfigOcorrências")
                sheetOccT.createRow(0).createCell(0).setCellValue("Tipo")
                dao.getOccurrenceTypes().first().forEachIndexed { i, item -> sheetOccT.createRow(i+1).createCell(0).setCellValue(item.type) }
                updateProgress("ConfigOcorrências", 1.0f, 19)

                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    workbook.write(outputStream)
                    outputStream.close()
                }
                workbook.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportNotificationsTable(context: Context, dao: AppDao, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Notificações")
                val header = sheet.createRow(0)
                listOf("ID", "Apto", "Proprietário", "Dívida Total", "Ano da Dívida", "Status Acordo", "Arquivado", "N1", "N2", "N3", "Andamentos").forEachIndexed { i, s -> 
                    header.createCell(i).setCellValue(s) 
                }

                val delinquents = dao.getAllDelinquents().sortedBy { it.apartment }
                val allProgress = dao.getAllDelinquentProgress()
                val progressMap = allProgress.groupBy { it.delinquentId }

                delinquents.forEachIndexed { i, item ->
                    val row = sheet.createRow(i + 1)
                    row.createCell(0).setCellValue(item.id.toDouble())
                    row.createCell(1).setCellValue(item.apartment)
                    row.createCell(2).setCellValue(item.ownerName)
                    row.createCell(3).setCellValue(item.totalDebt)
                    row.createCell(4).setCellValue(item.registrationDate) // Ano da dívida
                    row.createCell(5).setCellValue(if(item.hasMadeAgreement) "Acordo Feito" else "Pendente")
                    row.createCell(6).setCellValue(if(item.isArchived) "Sim" else "Não")
                    row.createCell(7).setCellValue(item.notification1Date ?: "N/A")
                    row.createCell(8).setCellValue(item.notification2Date ?: "N/A")
                    row.createCell(9).setCellValue(item.notification3Date ?: "N/A")
                    
                    val progressList = progressMap[item.id] ?: emptyList()
                    val progressText = progressList.sortedBy { it.date }.joinToString("\n") { 
                        "${it.date}: ${it.description}" 
                    }
                    row.createCell(10).setCellValue(progressText)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importNotificationsTable(context: Context, dao: AppDao, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            var successCount = 0
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext "Não foi possível abrir o arquivo."
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheet("Notificações") ?: return@withContext "Aba 'Notificações' não encontrada no arquivo."
                
                val currentDelinquents = dao.getAllDelinquents()
                val allExistingProgress = dao.getAllDelinquentProgress()
                val progressByApto = allExistingProgress.groupBy { prog -> 
                    currentDelinquents.find { it.id == prog.delinquentId }?.apartment ?: ""
                }
                
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    try {
                        val idCell = row.getCell(0)
                        val id = if (idCell != null && idCell.cellType == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            idCell.numericCellValue.toLong()
                        } else {
                            System.currentTimeMillis() + i
                        }

                        val aptoCell = row.getCell(1)
                        if (aptoCell == null || aptoCell.toString().isBlank()) continue
                        
                        val apto = aptoCell.toString().replace(".0", "").trim()
                        val nome = try { row.getCell(2).stringCellValue } catch(e: Exception) { 
                            errors.add("Linha ${i + 1}: Nome inválido para o apto $apto")
                            continue 
                        }
                        val divida = try { row.getCell(3).numericCellValue } catch(e: Exception) { 
                            errors.add("Linha ${i + 1}: Valor da dívida inválido para o apto $apto")
                            continue 
                        }
                        val ano = try { row.getCell(4).toString().replace(".0", "") } catch(e: Exception) { "" }
                        val statusStr = try { row.getCell(5).stringCellValue } catch(_: Exception) { "" }
                        val status = statusStr == "Acordo Feito"
                        val arquivado = try { row.getCell(6).stringCellValue == "Sim" } catch(_: Exception) { false }
                        
                        val n1 = try { row.getCell(7)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                        val n2 = try { row.getCell(8)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                        val n3 = try { row.getCell(9)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                        
                        val existing = currentDelinquents.find { it.apartment == apto }
                        val finalId = if (idCell != null && idCell.cellType == org.apache.poi.ss.usermodel.CellType.NUMERIC) id else (existing?.id ?: id)
                        
                        val delinquent = DelinquentEntity(
                            id = finalId,
                            apartment = apto,
                            ownerName = nome,
                            totalDebt = divida,
                            registrationDate = ano,
                            notification1Date = n1,
                            notification2Date = n2,
                            notification3Date = n3,
                            hasMadeAgreement = status,
                            isArchived = arquivado || (existing?.isArchived ?: false)
                        )
                        
                        dao.insertDelinquent(delinquent)
                        successCount++
                        
                        // Importar Andamentos (Coluna 10)
                        val andamentosStr = try { row.getCell(10)?.toString() ?: "" } catch(_: Exception) { "" }
                        if (andamentosStr.isNotBlank()) {
                            val lines = andamentosStr.split("\n")
                            val existingProgressForApto = progressByApto[apto] ?: emptyList()
                            
                            lines.forEach { line ->
                                if (line.contains(": ")) {
                                    val parts = line.split(": ", limit = 2)
                                    if (parts.size == 2) {
                                        val date = parts[0].trim()
                                        val desc = parts[1].trim()
                                        
                                        if (existingProgressForApto.none { it.date == date && it.description == desc }) {
                                            dao.insertDelinquentProgress(DelinquentProgressEntity(
                                                delinquentId = finalId,
                                                date = date,
                                                description = desc
                                            ))
                                        }
                                    }
                                }
                            }
                        }
                    } catch(e: Exception) {
                        errors.add("Linha ${i + 1}: Erro inesperado: ${e.message}")
                    }
                }
                workbook.close()
                inputStream.close()
            } catch (e: Exception) {
                return@withContext "Erro ao processar arquivo: ${e.message}"
            }

            val summary = StringBuilder("Importação concluída.\nSucessos: $successCount\nFalhas: ${errors.size}")
            if (errors.isNotEmpty()) {
                summary.append("\n\nDetalhes dos erros:\n")
                errors.forEach { summary.append("- $it\n") }
            }
            summary.toString()
        }
    }

    suspend fun importDatabase(context: Context, dao: AppDao, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
                val workbook = XSSFWorkbook(inputStream)
                
                // Importar Acordos
                val sheetAgg = workbook.getSheet("Acordos")
                if (sheetAgg != null) {
                    for (i in 1..sheetAgg.lastRowNum) {
                        val row = sheetAgg.getRow(i) ?: continue
                        try {
                            val entity = AgreementEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                apartment = row.getCell(1).stringCellValue,
                                ownerName = row.getCell(2).stringCellValue,
                                totalDebt = row.getCell(3).numericCellValue,
                                date = row.getCell(4).stringCellValue,
                                installmentsCount = row.getCell(5).numericCellValue.toInt(),
                                installmentValue = row.getCell(6).numericCellValue,
                                isLawsuit = row.getCell(7).stringCellValue == "Sim",
                                isArchived = try { row.getCell(8).stringCellValue == "Sim" } catch(_: Exception) { false },
                                originalAgreementId = try { val v = row.getCell(9).numericCellValue.toLong(); if (v == -1L) null else v } catch(_: Exception) { null },
                                n1Date = try { row.getCell(10).stringCellValue.ifBlank { null } } catch(_: Exception) { null },
                                n2Date = try { row.getCell(11).stringCellValue.ifBlank { null } } catch(_: Exception) { null },
                                n3Date = try { row.getCell(12).stringCellValue.ifBlank { null } } catch(_: Exception) { null }
                            )
                            dao.insertAgreement(entity)
                        } catch(_: Exception) {}
                    }
                }

                // Importar Parcelas
                val sheetInst = workbook.getSheet("Parcelas")
                if (sheetInst != null) {
                    val instList = mutableListOf<InstallmentEntity>()
                    for (i in 1..sheetInst.lastRowNum) {
                        val row = sheetInst.getRow(i) ?: continue
                        try {
                            instList.add(InstallmentEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                agreementId = row.getCell(1).numericCellValue.toLong(),
                                number = row.getCell(2).numericCellValue.toInt(),
                                value = row.getCell(3).numericCellValue,
                                dueDate = row.getCell(4).stringCellValue,
                                isPaid = row.getCell(5).stringCellValue == "Sim"
                            ))
                        } catch(_: Exception) {}
                    }
                    if (instList.isNotEmpty()) dao.insertInstallments(instList)
                }

                // Importar Notificações
                val sheetNotif = workbook.getSheet("Notificações")
                if (sheetNotif != null) {
                    val allExistingProgress = dao.getAllDelinquentProgress()
                    val progressByDelinquentId = allExistingProgress.groupBy { it.delinquentId }
                    
                    for (i in 1..sheetNotif.lastRowNum) {
                        val row = sheetNotif.getRow(i) ?: continue
                        try {
                            val id = row.getCell(0).numericCellValue.toLong()
                            val apto = row.getCell(1).toString().replace(".0", "").trim()
                            val nome = row.getCell(2).stringCellValue
                            val divida = row.getCell(3).numericCellValue
                            val ano = try { row.getCell(4)?.toString()?.replace(".0", "") ?: "" } catch(e: Exception) { "" }
                            val statusStr = try { row.getCell(5)?.toString() ?: "" } catch(e: Exception) { "" }
                            val status = statusStr == "Acordo Feito" || statusStr == "Sim"
                            val arquivado = try { row.getCell(6)?.toString() == "Sim" } catch(e: Exception) { false }
                            
                            val n1 = try { row.getCell(7)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                            val n2 = try { row.getCell(8)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                            val n3 = try { row.getCell(9)?.toString()?.let { if (it == "N/A" || it.isBlank()) null else it } } catch(_: Exception) { null }
                            
                            dao.insertDelinquent(DelinquentEntity(
                                id = id,
                                apartment = apto,
                                ownerName = nome,
                                totalDebt = divida,
                                registrationDate = ano,
                                notification1Date = n1,
                                notification2Date = n2,
                                notification3Date = n3,
                                hasMadeAgreement = status,
                                isArchived = arquivado
                            ))

                            // Importar Andamentos (Coluna 10)
                            val andamentosStr = try { row.getCell(10)?.toString() ?: "" } catch(_: Exception) { "" }
                            if (andamentosStr.isNotBlank()) {
                                val lines = andamentosStr.split("\n")
                                val existingProgress = progressByDelinquentId[id] ?: emptyList()
                                
                                lines.forEach { line ->
                                    if (line.contains(": ")) {
                                        val parts = line.split(": ", limit = 2)
                                        if (parts.size == 2) {
                                            val date = parts[0].trim()
                                            val desc = parts[1].trim()
                                            
                                            if (existingProgress.none { it.date == date && it.description == desc }) {
                                                dao.insertDelinquentProgress(DelinquentProgressEntity(
                                                    delinquentId = id,
                                                    date = date,
                                                    description = desc
                                                ))
                                            }
                                        }
                                    }
                                }
                            }
                        } catch(e: Exception) {
                            // Log or skip
                        }
                    }
                }

                // Importar Ajuizados
                val sheetLaw = workbook.getSheet("Ajuizados")
                if (sheetLaw != null) {
                    for (i in 1..sheetLaw.lastRowNum) {
                        val row = sheetLaw.getRow(i) ?: continue
                        try {
                            dao.insertLawsuit(LawsuitEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                apartment = row.getCell(1).stringCellValue,
                                ownerName = row.getCell(2).stringCellValue,
                                processNumber = row.getCell(3).stringCellValue,
                                forum = row.getCell(4).stringCellValue,
                                totalDebt = row.getCell(5).numericCellValue,
                                registrationDate = row.getCell(6).stringCellValue,
                                status = row.getCell(7).stringCellValue,
                                successValue = try { row.getCell(8).numericCellValue } catch(_: Exception) { null },
                                isFinished = try { row.getCell(9).stringCellValue == "Sim" } catch(_: Exception) { false }
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Andamentos
                val sheetProg = workbook.getSheet("Andamentos")
                if (sheetProg != null) {
                    for (i in 1..sheetProg.lastRowNum) {
                        val row = sheetProg.getRow(i) ?: continue
                        try {
                            dao.insertLawsuitProgress(LawsuitProgressEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                lawsuitId = row.getCell(1).numericCellValue.toLong(),
                                date = row.getCell(2).stringCellValue,
                                description = row.getCell(3).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar AndamentosNotif
                val sheetProgNotif = workbook.getSheet("AndamentosNotif")
                if (sheetProgNotif != null) {
                    for (i in 1..sheetProgNotif.lastRowNum) {
                        val row = sheetProgNotif.getRow(i) ?: continue
                        try {
                            dao.insertDelinquentProgress(DelinquentProgressEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                delinquentId = row.getCell(1).numericCellValue.toLong(),
                                date = row.getCell(2).stringCellValue,
                                description = row.getCell(3).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar AndamentosAcordo
                val sheetProgAgg = workbook.getSheet("AndamentosAcordo")
                if (sheetProgAgg != null) {
                    for (i in 1..sheetProgAgg.lastRowNum) {
                        val row = sheetProgAgg.getRow(i) ?: continue
                        try {
                            dao.insertAgreementProgress(AgreementProgressEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                agreementId = row.getCell(1).numericCellValue.toLong(),
                                date = row.getCell(2).stringCellValue,
                                description = row.getCell(3).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Manutenções
                val sheetOS = workbook.getSheet("Manutenções")
                if (sheetOS != null) {
                    for (i in 1..sheetOS.lastRowNum) {
                        val row = sheetOS.getRow(i) ?: continue
                        try {
                            dao.insertServiceOrder(ServiceOrderEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                date = row.getCell(1).stringCellValue,
                                description = row.getCell(2).stringCellValue,
                                floor = row.getCell(3).stringCellValue,
                                isInstallment = row.getCell(4).stringCellValue == "Sim",
                                totalValue = row.getCell(5).numericCellValue,
                                type = row.getCell(6).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar ParcelasOS
                val sheetOSInst = workbook.getSheet("ParcelasOS")
                if (sheetOSInst != null) {
                    val osInstList = mutableListOf<ServiceInstallmentEntity>()
                    for (i in 1..sheetOSInst.lastRowNum) {
                        val row = sheetOSInst.getRow(i) ?: continue
                        try {
                            osInstList.add(ServiceInstallmentEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                serviceOrderId = row.getCell(1).numericCellValue.toLong(),
                                number = row.getCell(2).numericCellValue.toInt(),
                                value = row.getCell(3).numericCellValue,
                                dueDate = row.getCell(4).stringCellValue,
                                isPaid = row.getCell(5).stringCellValue == "Sim"
                            ))
                        } catch(_: Exception) {}
                    }
                    if (osInstList.isNotEmpty()) dao.insertServiceInstallments(osInstList)
                }

                // Importar Historico
                val sheetHist = workbook.getSheet("Historico")
                if (sheetHist != null) {
                    for (i in 1..sheetHist.lastRowNum) {
                        val row = sheetHist.getRow(i) ?: continue
                        try {
                            dao.insertHistory(DelinquencyHistoryEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                apartment = row.getCell(1).stringCellValue,
                                ownerName = row.getCell(2).stringCellValue,
                                eventType = row.getCell(3).stringCellValue,
                                description = row.getCell(4).stringCellValue,
                                date = row.getCell(5).stringCellValue,
                                value = row.getCell(6).numericCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Ocorrências
                workbook.getSheet("Ocorrências")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            dao.insertOccurrence(OccurrenceEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                title = row.getCell(1).stringCellValue,
                                apartment = row.getCell(2).stringCellValue,
                                status = row.getCell(3).stringCellValue,
                                createdByUsername = row.getCell(4).stringCellValue,
                                date = row.getCell(5).stringCellValue,
                                type = row.getCell(6).stringCellValue,
                                occurrenceType = try { row.getCell(7).stringCellValue } catch(_: Exception) { "" },
                                isUrgent = try { row.getCell(8).stringCellValue == "Sim" } catch(_: Exception) { false }
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Mensagens Ocorrência
                workbook.getSheet("MensagensOcorrência")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            dao.insertOccurrenceMessage(OccurrenceMessageEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                occurrenceId = row.getCell(1).numericCellValue.toLong(),
                                senderUsername = row.getCell(2).stringCellValue,
                                text = row.getCell(3).stringCellValue,
                                date = row.getCell(4).stringCellValue,
                                isCouncilOnly = try { row.getCell(5).stringCellValue == "Sim" } catch(_: Exception) { false },
                                isSindicoOnly = try { row.getCell(6).stringCellValue == "Sim" } catch(_: Exception) { false },
                                isRead = try { row.getCell(7).stringCellValue == "Sim" } catch(_: Exception) { false },
                                isVotingClosed = try { row.getCell(8).stringCellValue == "Sim" } catch(_: Exception) { false },
                                isBudget = try { row.getCell(9).stringCellValue == "Sim" } catch(_: Exception) { false }
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Anexos Ocorrência
                workbook.getSheet("AnexosOcorrência")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            dao.insertAttachment(OccurrenceAttachmentEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                messageId = row.getCell(1).numericCellValue.toLong(),
                                fileName = row.getCell(2).stringCellValue,
                                filePath = row.getCell(3).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Votos Anexos
                workbook.getSheet("VotosAnexos")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            dao.insertAttachmentVote(OccurrenceAttachmentVoteEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                attachmentId = row.getCell(1).numericCellValue.toLong(),
                                username = row.getCell(2).stringCellValue
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Logs Ocorrência
                workbook.getSheet("LogsOcorrência")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            dao.insertOccurrenceLog(OccurrenceLogEntity(
                                id = row.getCell(0).numericCellValue.toLong(),
                                occurrenceId = row.getCell(1).numericCellValue.toLong(),
                                username = row.getCell(2).stringCellValue,
                                action = row.getCell(3).stringCellValue,
                                timestamp = row.getCell(4).numericCellValue.toLong()
                            ))
                        } catch(_: Exception) {}
                    }
                }

                // Importar Configurações
                workbook.getSheet("ConfigAndares")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        sheet.getRow(i)?.getCell(0)?.stringCellValue?.let { if(it.isNotBlank()) dao.insertFloor(FloorEntity(it)) }
                    }
                }
                workbook.getSheet("ConfigServiços")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        sheet.getRow(i)?.getCell(0)?.stringCellValue?.let { if(it.isNotBlank()) dao.insertServiceDescription(ServiceDescriptionEntity(it)) }
                    }
                }
                workbook.getSheet("ConfigStatusProc")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        sheet.getRow(i)?.getCell(0)?.stringCellValue?.let { if(it.isNotBlank()) dao.insertProcessStatus(ProcessStatusEntity(it)) }
                    }
                }
                workbook.getSheet("ConfigOcorrências")?.let { sheet ->
                    for (i in 1..sheet.lastRowNum) {
                        sheet.getRow(i)?.getCell(0)?.stringCellValue?.let { if(it.isNotBlank()) dao.insertOccurrenceType(OccurrenceTypeEntity(it)) }
                    }
                }

                workbook.close()
                inputStream.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportAllProgressTable(context: Context, dao: AppDao, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Andamentos")
                val header = sheet.createRow(0)
                listOf("Apto", "Data", "Descrição", "Tipo").forEachIndexed { i, s -> 
                    header.createCell(i).setCellValue(s) 
                }

                val delinquents = dao.getAllDelinquents()
                val agreements = dao.getAllAgreements()
                val notifProgress = dao.getAllDelinquentProgress()
                val aggProgress = dao.getAllAgreementProgress()

                var rowIdx = 1
                notifProgress.sortedBy { prog ->
                    delinquents.find { it.id == prog.delinquentId }?.apartment ?: ""
                }.forEach { prog ->
                    val apto = delinquents.find { it.id == prog.delinquentId }?.apartment ?: "N/A"
                    val row = sheet.createRow(rowIdx++)
                    row.createCell(0).setCellValue(apto)
                    row.createCell(1).setCellValue(prog.date)
                    row.createCell(2).setCellValue(prog.description)
                    row.createCell(3).setCellValue("Notificação")
                }

                aggProgress.sortedBy { prog ->
                    agreements.find { it.id == prog.agreementId }?.apartment ?: ""
                }.forEach { prog ->
                    val apto = agreements.find { it.id == prog.agreementId }?.apartment ?: "N/A"
                    val row = sheet.createRow(rowIdx++)
                    row.createCell(0).setCellValue(apto)
                    row.createCell(1).setCellValue(prog.date)
                    row.createCell(2).setCellValue(prog.description)
                    row.createCell(3).setCellValue("Acordo")
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importAllProgressTable(context: Context, dao: AppDao, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            var successCount = 0
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext "Não foi possível abrir o arquivo."
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)
                
                val delinquents = dao.getAllDelinquents()
                val agreements = dao.getAllAgreements()
                val existingNotifProgress = dao.getAllDelinquentProgress()
                val existingAggProgress = dao.getAllAgreementProgress()

                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    try {
                        val apto = row.getCell(0)?.toString()?.replace(".0", "")?.trim() ?: continue
                        if (apto.isBlank()) continue
                        
                        val date = row.getCell(1)?.toString() ?: ""
                        val desc = row.getCell(2)?.toString() ?: ""
                        val type = row.getCell(3)?.toString() ?: ""

                        if (type.contains("Notificação", ignoreCase = true)) {
                            val del = delinquents.find { it.apartment == apto }
                            if (del != null) {
                                if (existingNotifProgress.none { it.delinquentId == del.id && it.date == date && it.description == desc }) {
                                    dao.insertDelinquentProgress(DelinquentProgressEntity(delinquentId = del.id, date = date, description = desc))
                                    successCount++
                                }
                            } else {
                                errors.add("Linha ${i+1}: Unidade $apto não encontrada em Notificações.")
                            }
                        } else if (type.contains("Acordo", ignoreCase = true)) {
                            val agg = agreements.find { it.apartment == apto }
                            if (agg != null) {
                                if (existingAggProgress.none { it.agreementId == agg.id && it.date == date && it.description == desc }) {
                                    dao.insertAgreementProgress(AgreementProgressEntity(agreementId = agg.id, date = date, description = desc))
                                    successCount++
                                }
                            } else {
                                errors.add("Linha ${i+1}: Unidade $apto não encontrada em Acordos.")
                            }
                        }
                    } catch (e: Exception) {
                        errors.add("Linha ${i+1}: Erro inesperado.")
                    }
                }
                workbook.close()
            } catch (e: Exception) {
                return@withContext "Erro ao processar: ${e.message}"
            }
            val res = "Importação de andamentos concluída.\nSucessos: $successCount\nFalhas: ${errors.size}"
            if (errors.isNotEmpty()) res + "\n\nPrimeiros erros:\n" + errors.take(10).joinToString("\n") { "- $it" } else res
        }
    }
}

fun cancelAppNotification(context: Context, notificationId: Int) {
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    } catch (_: Exception) {}
}

fun showAppNotification(context: Context, title: String, message: String, notificationId: Int = 0) {
    val channelId = "condsuites_notifications_channel"
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "CondSuites Notificações",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações de Ocorrências do CondSuites"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val idToUse = if (notificationId != 0) notificationId else System.currentTimeMillis().toInt()
    notificationManager.notify(idToUse, notificationBuilder.build())
}

class CondSuitesFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "CondSuites"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Nova ocorrência registrada."
        val senderUsername = remoteMessage.data["senderUsername"]
        val occurrenceIdStr = remoteMessage.data["occurrenceId"]

        val currentUsername = UserPreferences.getCurrentSessionUser(this)
        if (!senderUsername.isNullOrEmpty() && senderUsername == currentUsername) {
            return
        }

        val notifId = occurrenceIdStr?.toIntOrNull() ?: (title + body).hashCode()
        showAppNotification(this, title, body, notifId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}

fun sendFcmPushNotification(
    dao: AppDao?, 
    title: String, 
    body: String, 
    senderUsername: String? = null, 
    occurrenceId: Long? = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        var status = "Falha"
        try {
            val safeTitle = title.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            val safeBody = body.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
            val safeSender = (senderUsername ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
            val safeOccId = (occurrenceId ?: 0L).toString()

            val url = URL("https://fcm.googleapis.com/fcm/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "key=BIMRZzMow5QjcqslRr5kSHKZXxjQ-uZYXwllsajDgy_jlU50vwO9fvMvoh8RQrjl-TGDpB6WjoHy4hUCtYcMXbQ")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonInputString = """
                {
                  "to": "/topics/occurrences",
                  "priority": "high",
                  "content_available": true,
                  "data": {
                    "title": "$safeTitle",
                    "body": "$safeBody",
                    "senderUsername": "$safeSender",
                    "occurrenceId": "$safeOccId"
                  }
                }
            """.trimIndent()

            conn.outputStream.use { os ->
                val input = jsonInputString.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            status = if (responseCode == 200) "Sucesso (200 OK)" else "Falha (Código $responseCode)"
        } catch (e: Exception) {
            status = "Erro: ${e.localizedMessage ?: "Desconhecido"}"
        } finally {
            if (dao != null) {
                try {
                    dao.insertNotificationLog(NotificationLogEntity(title = title, message = body, status = status))
                } catch (_: Exception) {}
            }
        }
    }
}

object FirestoreSyncManager {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }

    suspend fun touchParentOccurrence(dao: AppDao, occurrenceId: Long) {
        try {
            val occ = dao.getOccurrenceEntityById(occurrenceId)
            if (occ != null) {
                dao.updateOccurrence(occ)
            }
        } catch (_: Exception) {}
    }

    fun syncOccurrence(occ: OccurrenceEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("occurrences").document(occ.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(occ)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced occurrence ${occ.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync occurrence ${occ.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncOccurrence: ${e.message}")
        }
    }

    fun syncOccurrenceMessage(msg: OccurrenceMessageEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("occurrence_messages").document(msg.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(msg)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced occurrence message ${msg.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync occurrence message ${msg.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncOccurrenceMessage: ${e.message}")
        }
    }

    fun syncAgreement(agg: AgreementEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("agreements").document(agg.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(agg)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced agreement ${agg.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync agreement ${agg.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncAgreement: ${e.message}")
        }
    }

    fun syncDelinquent(del: DelinquentEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("delinquents").document(del.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(del)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced delinquent ${del.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync delinquent ${del.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncDelinquent: ${e.message}")
        }
    }

    fun syncLawsuit(lawsuit: LawsuitEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("lawsuits").document(lawsuit.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(lawsuit)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced lawsuit ${lawsuit.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync lawsuit ${lawsuit.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncLawsuit: ${e.message}")
        }
    }

    fun syncServiceOrder(order: ServiceOrderEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("service_orders").document(order.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(order)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced service order ${order.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync service order ${order.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncServiceOrder: ${e.message}")
        }
    }

    fun syncContract(contract: ContractEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("contracts").document(contract.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(contract)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced contract ${contract.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync contract ${contract.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncContract: ${e.message}")
        }
    }

    fun syncUser(user: UserEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docId = user.username.lowercase().trim()
            if (docId.isBlank()) return
            val docRef = db.collection("users").document(docId)
            val task = if (isDelete) docRef.delete() else docRef.set(user)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced user ${user.username}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync user ${user.username}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncUser: ${e.message}")
        }
    }

    fun syncOvertime(overtime: OvertimeEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("overtime").document(overtime.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(overtime)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced overtime ${overtime.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync overtime ${overtime.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncOvertime: ${e.message}")
        }
    }

    fun syncUnit(unit: UnitEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val fixedId = if (unit.id != 0L) unit.id else getUnitIdForApartment(unit.apartment)
            val docRef = db.collection("units").document(fixedId.toString())
            val unitToSave = unit.copy(id = fixedId)
            val task = if (isDelete) docRef.delete() else docRef.set(unitToSave)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced unit ${unit.apartment} ($fixedId)")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync unit ${unit.apartment}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncUnit: ${e.message}")
        }
    }

    fun syncAttachment(att: OccurrenceAttachmentEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("occurrence_attachments").document(att.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(att)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced attachment ${att.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync attachment ${att.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncAttachment: ${e.message}")
        }
    }

    fun syncAttachmentVote(vote: OccurrenceAttachmentVoteEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("occurrence_attachment_votes").document("${vote.attachmentId}_${vote.username}")
            val task = if (isDelete) docRef.delete() else docRef.set(vote)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced vote ${vote.attachmentId} by ${vote.username}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync vote: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncAttachmentVote: ${e.message}")
        }
    }

    fun pullAllFromCloud(dao: AppDao, scope: CoroutineScope) {
        val db = firestore ?: return
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Pull occurrences
                try {
                    val occTask = db.collection("occurrences").get()
                    val occSnap = com.google.android.gms.tasks.Tasks.await(occTask, 15, TimeUnit.SECONDS)
                    for (doc in occSnap.documents) {
                        val occ = doc.toObject(OccurrenceEntity::class.java)
                        if (occ != null) dao.insertOccurrenceReplace(occ)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling occurrences: ${e.message}")
                }

                // 2. Pull messages (guaranteeing messages exist in Room before attachments)
                try {
                    val msgTask = db.collection("occurrence_messages").get()
                    val msgSnap = com.google.android.gms.tasks.Tasks.await(msgTask, 15, TimeUnit.SECONDS)
                    for (doc in msgSnap.documents) {
                        val msg = doc.toObject(OccurrenceMessageEntity::class.java)
                        if (msg != null) dao.insertOccurrenceMessageReplace(msg)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling messages: ${e.message}")
                }

                // 3. Pull attachments (parent messages now exist in Room)
                try {
                    val attTask = db.collection("occurrence_attachments").get()
                    val attSnap = com.google.android.gms.tasks.Tasks.await(attTask, 15, TimeUnit.SECONDS)
                    for (doc in attSnap.documents) {
                        val att = doc.toObject(OccurrenceAttachmentEntity::class.java)
                        if (att != null) dao.insertAttachmentReplace(att)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling attachments: ${e.message}")
                }

                // 4. Pull votes
                try {
                    val voteTask = db.collection("occurrence_attachment_votes").get()
                    val voteSnap = com.google.android.gms.tasks.Tasks.await(voteTask, 15, TimeUnit.SECONDS)
                    for (doc in voteSnap.documents) {
                        val vote = doc.toObject(OccurrenceAttachmentVoteEntity::class.java)
                        if (vote != null) dao.insertAttachmentVote(vote)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling votes: ${e.message}")
                }

                // 5. Touch parent occurrences to force Room Flow observers to update UI immediately
                try {
                    val allMsgs = dao.getAllOccurrenceMessages()
                    allMsgs.map { it.occurrenceId }.distinct().forEach { occId ->
                        if (occId != 0L) touchParentOccurrence(dao, occId)
                    }
                } catch (_: Exception) {}

                // 6. Pull other collections
                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("agreements").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(AgreementEntity::class.java)
                        if (item != null) dao.insertAgreement(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("delinquents").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(DelinquentEntity::class.java)
                        if (item != null) dao.insertDelinquent(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("lawsuits").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(LawsuitEntity::class.java)
                        if (item != null) dao.insertLawsuit(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("service_orders").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(ServiceOrderEntity::class.java)
                        if (item != null) dao.insertServiceOrder(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("contracts").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(ContractEntity::class.java)
                        if (item != null) dao.insertContract(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("units").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(UnitEntity::class.java)
                        if (item != null && item.apartment.isNotBlank()) {
                            val fixedId = if (item.id != 0L) item.id else getUnitIdForApartment(item.apartment)
                            dao.insertUnit(item.copy(id = fixedId))
                        }
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("users").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(UserEntity::class.java)
                        if (item != null && item.username.isNotBlank()) {
                            val existing = dao.getUserByUsername(item.username)
                            if (existing == null) {
                                dao.insertUser(item)
                            } else {
                                dao.insertUser(item.copy(id = existing.id))
                            }
                        }
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("overtime").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(OvertimeEntity::class.java)
                        if (item != null) dao.insertOvertime(item)
                    }
                } catch (_: Exception) {}

                android.util.Log.d("FIRESTORE_SYNC", "Sequential pull from cloud finished successfully.")
            } catch (e: Exception) {
                android.util.Log.e("FIRESTORE_SYNC", "Error in sequential pull: ${e.message}", e)
            }
        }
    }

    fun startListening(context: Context, dao: AppDao, scope: CoroutineScope) {
        val db = firestore ?: return
        try {
            pullAllFromCloud(dao, scope)

            val isFirstMessagePass = java.util.concurrent.atomic.AtomicBoolean(true)

            db.collection("occurrences").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val occ = doc.toObject(OccurrenceEntity::class.java)
                                if (occ != null) {
                                    val existing = dao.getOccurrenceEntityById(occ.id)
                                    if (existing == null) {
                                        dao.insertOccurrenceReplace(occ)
                                        android.util.Log.d("FIRESTORE_SYNC", "Pulled new occurrence from cloud: ${occ.id}")
                                    } else if (existing != occ) {
                                        dao.updateOccurrence(occ)
                                        android.util.Log.d("FIRESTORE_SYNC", "Updated occurrence from cloud: ${occ.id}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error processing occurrence snapshot: ${e.message}", e)
                        }
                    }
                }
            }

            db.collection("occurrence_messages").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val msg = doc.toObject(OccurrenceMessageEntity::class.java)
                                if (msg != null) {
                                    val existingMessages = dao.getAllOccurrenceMessages()
                                    val existing = existingMessages.find { it.id == msg.id }
                                    if (existing == null) {
                                        dao.insertOccurrenceMessageReplace(msg)
                                        touchParentOccurrence(dao, msg.occurrenceId)
                                        if (!isFirstMessagePass.get()) {
                                            val currentUsername = UserPreferences.getCurrentSessionUser(context)
                                            if (msg.senderUsername != currentUsername) {
                                                val occ = dao.getOccurrenceEntityById(msg.occurrenceId)
                                                val occTitle = occ?.title ?: "Ocorrência"
                                                showAppNotification(
                                                    context,
                                                    "Nova mensagem em Ocorrências",
                                                    "$occTitle - ${msg.senderUsername}: ${msg.text}",
                                                    msg.occurrenceId.toInt()
                                                )
                                            }
                                        }
                                    } else if (existing != msg) {
                                        val msgToUpdate = if (existing.isRead) msg.copy(isRead = true) else msg
                                        dao.updateOccurrenceMessage(msgToUpdate)
                                        touchParentOccurrence(dao, msg.occurrenceId)
                                    }
                                }
                            }
                            isFirstMessagePass.set(false)
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("occurrence_attachments").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val att = doc.toObject(OccurrenceAttachmentEntity::class.java)
                                if (att != null) {
                                    dao.insertAttachmentReplace(att)
                                    if (att.occurrenceId != 0L) {
                                        touchParentOccurrence(dao, att.occurrenceId)
                                    } else {
                                        val messages = dao.getAllOccurrenceMessages()
                                        val msg = messages.find { it.id == att.messageId }
                                        if (msg != null) {
                                            touchParentOccurrence(dao, msg.occurrenceId)
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("occurrence_attachment_votes").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val vote = doc.toObject(OccurrenceAttachmentVoteEntity::class.java)
                                if (vote != null) {
                                    dao.insertAttachmentVote(vote)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("agreements").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val agg = doc.toObject(AgreementEntity::class.java)
                                if (agg != null) {
                                    val existing = dao.getAllAgreements().find { it.id == agg.id }
                                    if (existing == null) {
                                        dao.insertAgreement(agg)
                                    } else if (existing != agg) {
                                        dao.updateAgreement(agg)
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("delinquents").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val del = doc.toObject(DelinquentEntity::class.java)
                                if (del != null) {
                                    val existing = dao.getAllDelinquents().find { it.id == del.id }
                                    if (existing == null) {
                                        dao.insertDelinquent(del)
                                    } else if (existing != del) {
                                        dao.updateDelinquent(del)
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("lawsuits").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(LawsuitEntity::class.java)
                                if (item != null) {
                                    dao.insertLawsuit(item)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("service_orders").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(ServiceOrderEntity::class.java)
                                if (item != null) {
                                    dao.insertServiceOrder(item)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("contracts").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(ContractEntity::class.java)
                                if (item != null) {
                                    dao.insertContract(item)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("units").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(UnitEntity::class.java)
                                if (item != null && item.apartment.isNotBlank()) {
                                    val fixedId = if (item.id != 0L) item.id else getUnitIdForApartment(item.apartment)
                                    val unitWithFixedId = item.copy(id = fixedId)

                                    val existingList = dao.getAllUnitsList()
                                    val existingDuplicates = existingList.filter {
                                        it.apartment.equals(unitWithFixedId.apartment, ignoreCase = true) && it.id != fixedId
                                    }
                                    existingDuplicates.forEach { dao.deleteUnit(it.id) }

                                    dao.insertUnit(unitWithFixedId)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("users").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val cloudUsernames = mutableSetOf<String>()
                            for (doc in snapshot.documents) {
                                val user = doc.toObject(UserEntity::class.java)
                                if (user != null && user.username.isNotBlank()) {
                                    cloudUsernames.add(user.username.lowercase().trim())
                                    val existing = dao.getUserByUsername(user.username)
                                    if (existing == null) {
                                        dao.insertUser(user)
                                        android.util.Log.d("FIRESTORE_SYNC", "Pulled new user from cloud: ${user.username}")
                                    } else if (existing != user) {
                                        dao.insertUser(user.copy(id = existing.id))
                                        android.util.Log.d("FIRESTORE_SYNC", "Updated user from cloud: ${user.username}")
                                    }
                                }
                            }

                            val localUsers = dao.getAllUsersList()
                            for (localUser in localUsers) {
                                val localName = localUser.username.lowercase().trim()
                                if (!cloudUsernames.contains(localName) && snapshot.documents.isNotEmpty()) {
                                    if (localUser.username != "admin") {
                                        dao.deleteUser(localUser.id)
                                        android.util.Log.d("FIRESTORE_SYNC", "Deleted user removed from cloud: ${localUser.username}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error processing users snapshot: ${e.message}", e)
                        }
                    }
                }
            }

            val isFirstOvertimePass = java.util.concurrent.atomic.AtomicBoolean(true)

            db.collection("overtime").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(OvertimeEntity::class.java)
                                if (item != null) {
                                    val existingList = dao.getAllOvertimeList()
                                    val existing = existingList.find { it.id == item.id }
                                    if (existing == null) {
                                        dao.insertOvertime(item)
                                        if (!isFirstOvertimePass.get()) {
                                            val currentUsername = UserPreferences.getCurrentSessionUser(context)
                                            if (item.employeeName != currentUsername && item.approvedByUsername != currentUsername) {
                                                val notifTitle = "Nova Hora Extra Lançada"
                                                val notifBody = "${item.employeeName} (${item.employeeRole}) - ${item.date} (${item.startTime} às ${item.endTime}, ${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h)"
                                                showAppNotification(context, notifTitle, notifBody, item.id.toInt())
                                            }
                                        }
                                    } else if (existing != item) {
                                        dao.updateOvertime(item)
                                    }
                                }
                            }
                            isFirstOvertimePass.set(false)
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error starting listeners: ${e.message}", e)
        }
    }
}
