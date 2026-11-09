package com.example.condsuites.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.condsuites.data.model.UserEntity
import java.io.PrintWriter
import java.io.StringWriter

object AppErrorHandler {
    var currentError by mutableStateOf<String?>(null)
    private var currentUserProvider: (() -> UserEntity?)? = null

    fun init(userProvider: () -> UserEntity?) {
        currentUserProvider = userProvider
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val user = currentUserProvider?.invoke()
            if (isTargetUser(user)) {
                val errorMsg = getStackTraceString(throwable)
                Handler(Looper.getMainLooper()).post {
                    showError(errorMsg)
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    try {
                        Looper.loop()
                    } catch (_: Throwable) {}
                }
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun isTargetUser(user: UserEntity?): Boolean {
        if (user == null) return false
        val username = user.username.trim()
        val role = user.role.trim()
        return username.equals("admin", ignoreCase = true) ||
                username.equals("samuel", ignoreCase = true) ||
                role.equals("ADMIN", ignoreCase = true)
    }

    fun isTargetUser(username: String?): Boolean {
        if (username.isNullOrBlank()) return false
        val u = username.trim()
        return u.equals("admin", ignoreCase = true) || u.equals("samuel", ignoreCase = true)
    }

    fun showError(error: String) {
        currentError = error
    }

    fun showError(throwable: Throwable) {
        showError(getStackTraceString(throwable))
    }

    fun clearError() {
        currentError = null
    }

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        return sw.toString()
    }
}

@Composable
fun ErrorPopupDialog(
    errorText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var textToCopyOrPaste by remember(errorText) { mutableStateOf(errorText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️ Erro no Aplicativo",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ocorreu um erro no aplicativo. Você pode visualizar, editar ou copiar o erro abaixo para colar onde desejar:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SelectionContainer {
                            OutlinedTextField(
                                value = textToCopyOrPaste,
                                onValueChange = { textToCopyOrPaste = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                label = { Text("Detalhes do Erro (Copiar / Colar)") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Erro do Aplicativo", textToCopyOrPaste)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Erro copiado para a área de transferência! Prontinho para colar.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao copiar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Copiar / Colar Erro")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
