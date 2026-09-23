package com.example.condsuites.utils

import android.content.Context
import android.net.Uri
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AgreementEntity
import com.example.condsuites.data.model.AgreementProgressEntity
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.DelinquentEntity
import com.example.condsuites.data.model.DelinquentProgressEntity
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.InstallmentEntity
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.LawsuitProgressEntity
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.OccurrenceAttachmentVoteEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceLogEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.ServiceInstallmentEntity
import com.example.condsuites.data.model.ServiceOrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    row.createCell(4).setCellValue(item.registrationDate)
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
                        }
                    }
                }

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
