package com.example.condsuites.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.condsuites.data.model.FinanceTransactionEntity
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AgreementEntity
import com.example.condsuites.data.model.AgreementProgressEntity
import com.example.condsuites.data.model.ContractEntity
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.DelinquentEntity
import com.example.condsuites.data.model.DelinquentProgressEntity
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.InstallmentEntity
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.LawsuitProgressEntity
import com.example.condsuites.data.model.NotificationLogEntity
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.OccurrenceAttachmentVoteEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceLogEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.OvertimeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.ServiceInstallmentEntity
import com.example.condsuites.data.model.ServiceOrderEntity
import com.example.condsuites.data.model.UnitEntity
import com.example.condsuites.data.model.UserEntity

@Database(
    entities = [
        AgreementEntity::class, InstallmentEntity::class, ServiceOrderEntity::class,
        ServiceInstallmentEntity::class, DelinquentEntity::class, LawsuitEntity::class,
        DelinquencyHistoryEntity::class, LawsuitProgressEntity::class, DelinquentProgressEntity::class,
        AgreementProgressEntity::class, UserEntity::class, OccurrenceEntity::class,
        OccurrenceLogEntity::class, OccurrenceMessageEntity::class, FloorEntity::class,
        ServiceDescriptionEntity::class, ProcessStatusEntity::class, OccurrenceTypeEntity::class,
        OccurrenceAttachmentEntity::class, OccurrenceAttachmentVoteEntity::class, ContractEntity::class,
        NotificationLogEntity::class, UnitEntity::class, OvertimeEntity::class, FinanceTransactionEntity::class
    ],
    version = 40,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "condsuites_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
