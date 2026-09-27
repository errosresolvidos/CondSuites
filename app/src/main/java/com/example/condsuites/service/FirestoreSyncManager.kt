package com.example.condsuites.service

import android.content.Context
import android.net.Uri
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AgreementEntity
import com.example.condsuites.data.model.ContractEntity
import com.example.condsuites.data.model.DelinquentEntity
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.FinanceTransactionEntity
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.OccurrenceAttachmentVoteEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.OvertimeEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.ServiceOrderEntity
import com.example.condsuites.data.model.UnitEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.utils.getUnitIdForApartment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

    fun syncDelinquencyHistory(item: DelinquencyHistoryEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("delinquency_history").document(item.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(item)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced delinquency history ${item.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync delinquency history ${item.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncDelinquencyHistory: ${e.message}")
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

    fun syncTransaction(tx: FinanceTransactionEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            val docRef = db.collection("finance_transactions").document(tx.id.toString())
            val task = if (isDelete) docRef.delete() else docRef.set(tx)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced finance transaction ${tx.id}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync finance transaction ${tx.id}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncTransaction: ${e.message}")
        }
    }

    fun syncServiceDescription(desc: ServiceDescriptionEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            if (desc.description.isBlank()) return
            val docId = Uri.encode(desc.description)
            val docRef = db.collection("config_service_descriptions").document(docId)
            val task = if (isDelete) docRef.delete() else docRef.set(desc)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced service description ${desc.description}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync service description ${desc.description}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncServiceDescription: ${e.message}")
        }
    }

    fun syncOccurrenceType(type: OccurrenceTypeEntity, isDelete: Boolean = false) {
        try {
            val db = firestore ?: return
            if (type.type.isBlank()) return
            val docId = Uri.encode(type.type)
            val docRef = db.collection("config_occurrence_types").document(docId)
            val task = if (isDelete) docRef.delete() else docRef.set(type)
            task.addOnSuccessListener {
                android.util.Log.d("FIRESTORE_SYNC", "Successfully synced occurrence type ${type.type}")
            }.addOnFailureListener { e ->
                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync occurrence type ${type.type}: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error in syncOccurrenceType: ${e.message}")
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

                // 2. Pull messages
                try {
                    val msgTask = db.collection("occurrence_messages").get()
                    val msgSnap = com.google.android.gms.tasks.Tasks.await(msgTask, 15, TimeUnit.SECONDS)
                    for (doc in msgSnap.documents) {
                        val msg = doc.toObject(OccurrenceMessageEntity::class.java)
                        if (msg != null) {
                            try {
                                if (dao.getOccurrenceEntityById(msg.occurrenceId) == null) {
                                    val parentDoc = com.google.android.gms.tasks.Tasks.await(
                                        db.collection("occurrences").document(msg.occurrenceId.toString()).get()
                                    )
                                    val parentOcc = parentDoc.toObject(OccurrenceEntity::class.java)
                                    if (parentOcc != null) dao.insertOccurrenceReplace(parentOcc)
                                }
                                dao.insertOccurrenceMessageReplace(msg)
                            } catch (e: Exception) {
                                android.util.Log.e("FIRESTORE_SYNC", "Error inserting message ${msg.id}: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling messages: ${e.message}")
                }

                // 3. Pull attachments
                try {
                    val attTask = db.collection("occurrence_attachments").get()
                    val attSnap = com.google.android.gms.tasks.Tasks.await(attTask, 15, TimeUnit.SECONDS)
                    for (doc in attSnap.documents) {
                        val att = doc.toObject(OccurrenceAttachmentEntity::class.java)
                        if (att != null) {
                            try {
                                if (dao.getOccurrenceMessageById(att.messageId) == null) {
                                    val parentMsgDoc = com.google.android.gms.tasks.Tasks.await(
                                        db.collection("occurrence_messages").document(att.messageId.toString()).get()
                                    )
                                    val parentMsg = parentMsgDoc.toObject(OccurrenceMessageEntity::class.java)
                                    if (parentMsg != null) {
                                        if (dao.getOccurrenceEntityById(parentMsg.occurrenceId) == null) {
                                            val parentOccDoc = com.google.android.gms.tasks.Tasks.await(
                                                db.collection("occurrences").document(parentMsg.occurrenceId.toString()).get()
                                            )
                                            val parentOcc = parentOccDoc.toObject(OccurrenceEntity::class.java)
                                            if (parentOcc != null) dao.insertOccurrenceReplace(parentOcc)
                                        }
                                        dao.insertOccurrenceMessageReplace(parentMsg)
                                    }
                                }
                                dao.insertAttachmentReplace(att)
                            } catch (e: Exception) {
                                android.util.Log.e("FIRESTORE_SYNC", "Error inserting attachment ${att.id}: ${e.message}")
                            }
                        }
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
                        if (vote != null) {
                            try {
                                if (dao.getAttachmentById(vote.attachmentId) == null) {
                                    val parentAttDoc = com.google.android.gms.tasks.Tasks.await(
                                        db.collection("occurrence_attachments").document(vote.attachmentId.toString()).get()
                                    )
                                    val parentAtt = parentAttDoc.toObject(OccurrenceAttachmentEntity::class.java)
                                    if (parentAtt != null) dao.insertAttachmentReplace(parentAtt)
                                }
                                dao.insertAttachmentVote(vote)
                            } catch (e: Exception) {
                                android.util.Log.e("FIRESTORE_SYNC", "Error inserting vote: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling votes: ${e.message}")
                }

                // 5. Touch parent occurrences
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
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("delinquency_history").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(DelinquencyHistoryEntity::class.java)
                        if (item != null) dao.insertHistory(item)
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

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("finance_transactions").get(), 10, TimeUnit.SECONDS)
                    for (doc in snap.documents) {
                        val item = doc.toObject(FinanceTransactionEntity::class.java)
                        if (item != null) dao.insertTransaction(item)
                    }
                } catch (_: Exception) {}

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("config_service_descriptions").get(), 10, TimeUnit.SECONDS)
                    val list = snap.toObjects(ServiceDescriptionEntity::class.java)
                    if (list.isNotEmpty()) {
                        for (item in list) {
                            if (item.description.isNotBlank()) dao.insertServiceDescription(item)
                        }
                    } else {
                        val defaults = listOf("TROCA DE DICTADOR", "TROCA DE BOTÃO", "TROCA DE VENTILADOR")
                        for (d in defaults) {
                            val entity = ServiceDescriptionEntity(description = d)
                            dao.insertServiceDescription(entity)
                            syncServiceDescription(entity)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling config_service_descriptions: ${e.message}")
                }

                try {
                    val snap = com.google.android.gms.tasks.Tasks.await(db.collection("config_occurrence_types").get(), 10, TimeUnit.SECONDS)
                    val list = snap.toObjects(OccurrenceTypeEntity::class.java)
                    if (list.isNotEmpty()) {
                        for (item in list) {
                            if (item.type.isNotBlank()) dao.insertOccurrenceType(item)
                        }
                    } else {
                        val defaults = listOf("Elevador Social", "Elevador de Serviço", "Piscina", "Jardim", "Vazamento", "Caixa D'água", "Câmeras", "Barulho", "Infiltração", "Limpeza")
                        for (t in defaults) {
                            val entity = OccurrenceTypeEntity(type = t)
                            dao.insertOccurrenceType(entity)
                            syncOccurrenceType(entity)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FIRESTORE_SYNC", "Error pulling config_occurrence_types: ${e.message}")
                }

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
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    val occId = dc.document.id.toLongOrNull()
                                    if (occId != null) {
                                        dao.deleteOccurrence(occId)
                                        dao.deleteTransactionsByRelatedId(occId)
                                    }
                                }
                            }
                            for (doc in snapshot.documents) {
                                val occ = doc.toObject(OccurrenceEntity::class.java)
                                if (occ != null) {
                                    dao.insertOccurrenceReplace(occ)
                                    android.util.Log.d("FIRESTORE_SYNC", "Synced occurrence from cloud: ${occ.id}")
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
                            for (dc in snapshot.documentChanges) {
                                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    val msgId = dc.document.id.toLongOrNull()
                                    if (msgId != null) {
                                        val msg = dao.getOccurrenceMessageById(msgId)
                                        if (msg != null) {
                                            dao.deleteOccurrenceMessage(msgId)
                                            touchParentOccurrence(dao, msg.occurrenceId)
                                        }
                                    }
                                }
                            }
                            for (doc in snapshot.documents) {
                                val msg = doc.toObject(OccurrenceMessageEntity::class.java)
                                if (msg != null) {
                                    try {
                                        if (dao.getOccurrenceEntityById(msg.occurrenceId) == null) {
                                            val parentDoc = com.google.android.gms.tasks.Tasks.await(
                                                db.collection("occurrences").document(msg.occurrenceId.toString()).get()
                                            )
                                            val parentOcc = parentDoc.toObject(OccurrenceEntity::class.java)
                                            if (parentOcc != null) {
                                                dao.insertOccurrenceReplace(parentOcc)
                                            }
                                        }

                                        val existing = dao.getOccurrenceMessageById(msg.id)
                                        if (existing == null) {
                                            dao.insertOccurrenceMessageReplace(msg)
                                            touchParentOccurrence(dao, msg.occurrenceId)
                                            if (!isFirstMessagePass.get()) {
                                                val currentUsername = UserPreferences.getCurrentSessionUser(context)
                                                if (msg.senderUsername != currentUsername) {
                                                    val occ = dao.getOccurrenceEntityById(msg.occurrenceId)
                                                    val occTitle = occ?.title ?: "Ocorrência"
                                                    NotificationUtils.showAppNotification(
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
                                    } catch (e: Exception) {
                                        android.util.Log.e("FIRESTORE_SYNC", "Error processing message ${msg.id}: ${e.message}", e)
                                    }
                                }
                            }
                            isFirstMessagePass.set(false)
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error in occurrence_messages snapshot: ${e.message}", e)
                        }
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
                                    try {
                                        if (dao.getOccurrenceMessageById(att.messageId) == null) {
                                            val parentMsgDoc = com.google.android.gms.tasks.Tasks.await(
                                                db.collection("occurrence_messages").document(att.messageId.toString()).get()
                                            )
                                            val parentMsg = parentMsgDoc.toObject(OccurrenceMessageEntity::class.java)
                                            if (parentMsg != null) {
                                                if (dao.getOccurrenceEntityById(parentMsg.occurrenceId) == null) {
                                                    val parentOccDoc = com.google.android.gms.tasks.Tasks.await(
                                                        db.collection("occurrences").document(parentMsg.occurrenceId.toString()).get()
                                                    )
                                                    val parentOcc = parentOccDoc.toObject(OccurrenceEntity::class.java)
                                                    if (parentOcc != null) dao.insertOccurrenceReplace(parentOcc)
                                                }
                                                dao.insertOccurrenceMessageReplace(parentMsg)
                                            }
                                        }
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
                                    } catch (e: Exception) {
                                        android.util.Log.e("FIRESTORE_SYNC", "Error processing attachment ${att.id}: ${e.message}", e)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error processing attachments snapshot: ${e.message}", e)
                        }
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
                                    try {
                                        if (dao.getAttachmentById(vote.attachmentId) == null) {
                                            val parentAttDoc = com.google.android.gms.tasks.Tasks.await(
                                                db.collection("occurrence_attachments").document(vote.attachmentId.toString()).get()
                                            )
                                            val parentAtt = parentAttDoc.toObject(OccurrenceAttachmentEntity::class.java)
                                            if (parentAtt != null) dao.insertAttachmentReplace(parentAtt)
                                        }
                                        dao.insertAttachmentVote(vote)
                                    } catch (e: Exception) {
                                        android.util.Log.e("FIRESTORE_SYNC", "Error processing vote: ${e.message}", e)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error processing votes snapshot: ${e.message}", e)
                        }
                    }
                }
            }

            db.collection("agreements").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val cloudIds = mutableSetOf<Long>()
                            for (doc in snapshot.documents) {
                                val agg = doc.toObject(AgreementEntity::class.java)
                                if (agg != null) {
                                    cloudIds.add(agg.id)
                                    val existing = dao.getAllAgreements().find { it.id == agg.id }
                                    if (existing == null) {
                                        dao.insertAgreement(agg)
                                    } else if (existing != agg) {
                                        dao.updateAgreement(agg)
                                    }
                                }
                            }
                            val localAgreements = dao.getAllAgreements()
                            for (localAgg in localAgreements) {
                                if (!cloudIds.contains(localAgg.id)) {
                                    dao.deleteAgreement(localAgg.id)
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
                            val cloudIds = mutableSetOf<Long>()
                            for (doc in snapshot.documents) {
                                val del = doc.toObject(DelinquentEntity::class.java)
                                if (del != null) {
                                    cloudIds.add(del.id)
                                    val existing = dao.getAllDelinquents().find { it.id == del.id }
                                    if (existing == null) {
                                        dao.insertDelinquent(del)
                                    } else if (existing != del) {
                                        dao.updateDelinquent(del)
                                    }
                                }
                            }
                            val localDelinquents = dao.getAllDelinquents()
                            for (localDel in localDelinquents) {
                                if (!cloudIds.contains(localDel.id)) {
                                    dao.deleteDelinquent(localDel.id)
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
                            val cloudIds = mutableSetOf<Long>()
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(LawsuitEntity::class.java)
                                if (item != null) {
                                    cloudIds.add(item.id)
                                    val existing = dao.getAllLawsuits().find { it.id == item.id }
                                    if (existing == null) {
                                        dao.insertLawsuit(item)
                                    } else if (existing != item) {
                                        dao.updateLawsuit(item)
                                    }
                                }
                            }
                            val localLawsuits = dao.getAllLawsuits()
                            for (localLawsuit in localLawsuits) {
                                if (!cloudIds.contains(localLawsuit.id)) {
                                    dao.deleteLawsuit(localLawsuit.id)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            db.collection("delinquency_history").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val cloudIds = mutableSetOf<Long>()
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(DelinquencyHistoryEntity::class.java)
                                if (item != null) {
                                    cloudIds.add(item.id)
                                    val existingList = dao.getAllHistory()
                                    val existing = existingList.find { it.id == item.id }
                                    if (existing == null) {
                                        dao.insertHistory(item)
                                    }
                                }
                            }
                            val localHistory = dao.getAllHistory()
                            for (localItem in localHistory) {
                                if (!cloudIds.contains(localItem.id)) {
                                    dao.deleteHistory(localItem.id)
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
                                                val notifBody = "${item.employeeName} (${item.employeeRole}) - ${item.date} (${item.startTime} às ${item.endTime}, ${String.format(java.util.Locale.getDefault(), "%.1f", item.totalHours)}h)"
                                                NotificationUtils.showAppNotification(context, notifTitle, notifBody, item.id.toInt())
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

            val isFirstFinancePass = java.util.concurrent.atomic.AtomicBoolean(true)

            db.collection("finance_transactions").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val cloudIds = mutableSetOf<Long>()
                            for (doc in snapshot.documents) {
                                val item = doc.toObject(FinanceTransactionEntity::class.java)
                                if (item != null) {
                                    cloudIds.add(item.id)
                                    val existing = dao.getTransactionById(item.id)
                                    if (existing == null) {
                                        dao.insertTransaction(item)
                                        android.util.Log.d("FIRESTORE_SYNC", "Pulled new finance transaction from cloud: ${item.id}")
                                        if (!isFirstFinancePass.get()) {
                                            val notifTitle = when {
                                                item.isPaid -> "Novo Lançamento Pago"
                                                item.type == "RECEIVABLE" -> "Nova Conta a Receber"
                                                else -> "Nova Conta a Pagar"
                                            }
                                            val notifBody = "${item.title} - R$ ${String.format(java.util.Locale.getDefault(), "%.2f", item.amount)}"
                                            NotificationUtils.showAppNotification(context, notifTitle, notifBody, item.id.toInt())
                                        }
                                    } else if (existing != item) {
                                        dao.updateTransaction(item)
                                        android.util.Log.d("FIRESTORE_SYNC", "Updated finance transaction from cloud: ${item.id}")
                                    }
                                }
                            }
                            val localTransactions = dao.getAllTransactionsList()
                            for (localTx in localTransactions) {
                                if (!cloudIds.contains(localTx.id) && snapshot.documents.isNotEmpty()) {
                                    dao.deleteTransaction(localTx.id)
                                    android.util.Log.d("FIRESTORE_SYNC", "Deleted finance transaction removed from cloud: ${localTx.id}")
                                }
                            }
                            isFirstFinancePass.set(false)
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error processing finance transactions snapshot: ${e.message}", e)
                        }
                    }
                }
            }

            db.collection("config_service_descriptions").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (dc in snapshot.documentChanges) {
                                val item = dc.document.toObject(ServiceDescriptionEntity::class.java)
                                if (item.description.isNotBlank()) {
                                    when (dc.type) {
                                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                            dao.insertServiceDescription(item)
                                        }
                                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                            dao.deleteServiceDescription(item.description)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error in service_descriptions listener: ${e.message}", e)
                        }
                    }
                }
            }

            db.collection("config_occurrence_types").addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (dc in snapshot.documentChanges) {
                                val item = dc.document.toObject(OccurrenceTypeEntity::class.java)
                                if (item.type.isNotBlank()) {
                                    when (dc.type) {
                                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                            dao.insertOccurrenceType(item)
                                        }
                                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                            dao.deleteOccurrenceType(item.type)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FIRESTORE_SYNC", "Error in occurrence_types listener: ${e.message}", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE_SYNC", "Error starting listeners: ${e.message}", e)
        }
    }
}
