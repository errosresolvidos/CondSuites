package com.example.condsuites.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.FinanceTransactionEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Suppress("UNUSED_PARAMETER")
fun ConfirmDeleteOccurrenceDialog(
    occurrence: OccurrenceEntity,
    dao: AppDao,
    currentUser: UserEntity,
    context: Context,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    var relatedTxs by remember { mutableStateOf<List<FinanceTransactionEntity>>(emptyList()) }

    LaunchedEffect(occurrence.id) {
        withContext(Dispatchers.IO) {
            relatedTxs = dao.getTransactionsByRelatedId(occurrence.id)
        }
    }

    val hasBudget = relatedTxs.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Ocorrência") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Deseja mesmo excluir a ocorrência \"${occurrence.title}\"? Esta ação não pode ser desfeita.")
                if (hasBudget) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ Atenção: Esta ocorrência possui orçamento(s) associado(s). A exclusão também removerá o(s) card(s) correspondente(s) em Contas.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        relatedTxs.forEach { tx ->
                            dao.deleteTransaction(tx.id)
                            FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                        }
                        dao.deleteTransactionsByRelatedId(occurrence.id)
                        dao.deleteOccurrence(occurrence.id)
                        FirestoreSyncManager.syncOccurrence(occurrence, isDelete = true)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
