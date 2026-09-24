package com.example.condsuites.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.condsuites.data.model.FinanceTransactionEntity
import com.example.condsuites.data.model.AgreementEntity
import com.example.condsuites.data.model.AgreementProgressEntity
import com.example.condsuites.data.model.AgreementWithInstallments
import com.example.condsuites.data.model.ContractEntity
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.DelinquentEntity
import com.example.condsuites.data.model.DelinquentProgressEntity
import com.example.condsuites.data.model.DelinquentWithProgress
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.InstallmentEntity
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.LawsuitProgressEntity
import com.example.condsuites.data.model.LawsuitWithProgress
import com.example.condsuites.data.model.NotificationLogEntity
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.OccurrenceAttachmentVoteEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceLogEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.OvertimeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.ServiceInstallmentEntity
import com.example.condsuites.data.model.ServiceOrderEntity
import com.example.condsuites.data.model.ServiceOrderWithInstallments
import com.example.condsuites.data.model.UnitEntity
import com.example.condsuites.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

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
    @Update suspend fun updateServiceOrder(order: ServiceOrderEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertServiceInstallments(installments: List<ServiceInstallmentEntity>)
    @Update suspend fun updateServiceInstallment(installment: ServiceInstallmentEntity)
    @Query("DELETE FROM service_installments WHERE serviceOrderId = :orderId") suspend fun deleteServiceInstallmentsByOrderId(orderId: Long)
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
    
    @Transaction
    @Query("SELECT * FROM occurrences WHERE id = :id")
    suspend fun getOccurrenceWithMessagesById(id: Long): OccurrenceWithMessages?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long
    @Query("SELECT * FROM occurrences WHERE id = :id") suspend fun getOccurrenceEntityById(id: Long): OccurrenceEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceReplace(occurrence: OccurrenceEntity): Long
    @Update suspend fun updateOccurrence(occurrence: OccurrenceEntity)
    @Query("DELETE FROM occurrences WHERE id = :id") suspend fun deleteOccurrenceOnly(id: Long)
    @Query("DELETE FROM occurrence_messages WHERE occurrenceId = :occId") suspend fun deleteOccurrenceMessagesByOccurrenceId(occId: Long)
    @Query("DELETE FROM occurrence_attachments WHERE occurrenceId = :occId") suspend fun deleteOccurrenceAttachmentsByOccurrenceId(occId: Long)

    @Transaction
    suspend fun deleteOccurrence(id: Long) {
        deleteOccurrenceMessagesByOccurrenceId(id)
        deleteOccurrenceAttachmentsByOccurrenceId(id)
        deleteOccurrenceOnly(id)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceMessage(message: OccurrenceMessageEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOccurrenceMessageReplace(message: OccurrenceMessageEntity): Long
    @Update suspend fun updateOccurrenceMessage(message: OccurrenceMessageEntity)
    @Query("SELECT * FROM occurrence_messages") suspend fun getAllOccurrenceMessages(): List<OccurrenceMessageEntity>
    @Query("SELECT * FROM occurrence_messages WHERE id = :id") suspend fun getOccurrenceMessageById(id: Long): OccurrenceMessageEntity?
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

    @Query("SELECT * FROM finance_transactions ORDER BY dueDate ASC") fun getAllTransactionsFlow(): Flow<List<FinanceTransactionEntity>>
    @Query("SELECT * FROM finance_transactions ORDER BY dueDate ASC") suspend fun getAllTransactionsList(): List<FinanceTransactionEntity>
    @Query("SELECT * FROM finance_transactions WHERE id = :id") suspend fun getTransactionById(id: Long): FinanceTransactionEntity?
    @Query("SELECT * FROM finance_transactions WHERE isPaid = 0 ORDER BY dueDate ASC") fun getPendingTransactionsFlow(): Flow<List<FinanceTransactionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransaction(transaction: FinanceTransactionEntity): Long
    @Update suspend fun updateTransaction(transaction: FinanceTransactionEntity)
    @Query("DELETE FROM finance_transactions WHERE id = :id") suspend fun deleteTransaction(id: Long)
}
