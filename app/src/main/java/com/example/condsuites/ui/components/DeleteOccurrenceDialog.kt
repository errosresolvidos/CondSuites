package com.example.condsuites.ui.components

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Ocorrência") },
        text = {
            Text("Deseja mesmo excluir a ocorrência \"${occurrence.title}\"? Esta ação não pode ser desfeita.")
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
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
