package com.example.condsuites.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.condsuites.data.model.MessageWithAttachments
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.UserEntity

@Composable
fun SendWhatsappToSindicoDialog(
    occWithMsgs: OccurrenceWithMessages,
    occNumber: Any,
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val occ = occWithMsgs.occurrence
    val messages = occWithMsgs.messages.sortedBy { it.message.id }

    val sindicos = remember(allUsers) {
        allUsers.filter { user ->
            user.role.equals("Síndico", ignoreCase = true) ||
                    user.role.contains("Síndico", ignoreCase = true) ||
                    user.role.contains("Sindico", ignoreCase = true)
        }
    }

    val initialPhone = remember(sindicos) {
        sindicos.firstOrNull { it.phone.isNotBlank() }?.phone ?: ""
    }

    var selectedPhone by remember { mutableStateOf(initialPhone) }
    var selectedSindicoUsername by remember {
        val firstSindicoWithPhone = sindicos.firstOrNull { it.phone.isNotBlank() }
        val defaultName = firstSindicoWithPhone?.name?.ifEmpty { firstSindicoWithPhone.username }
            ?: sindicos.firstOrNull()?.name?.ifEmpty { sindicos.firstOrNull()?.username ?: "" }
            ?: ""
        mutableStateOf(defaultName)
    }

    val formattedMsg = remember(occ, occNumber, messages) {
        buildWhatsappOccurrenceMessage(occ, occNumber, messages)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Enviar ao Síndico via WhatsApp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Conselho Fiscal • Notificação de Ocorrência",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                if (sindicos.size > 1) {
                    Text(
                        text = "Selecione o Síndico:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sindicos.forEach { sindico ->
                            val sindicoDisplayName = sindico.name.ifEmpty { sindico.username }
                            val isSelected = (selectedSindicoUsername == sindicoDisplayName)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSindicoUsername = sindicoDisplayName
                                    if (sindico.phone.isNotBlank()) {
                                        selectedPhone = sindico.phone
                                    }
                                },
                                label = {
                                    Text(
                                        text = sindicoDisplayName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedPhone,
                    onValueChange = { selectedPhone = it },
                    label = { Text("WhatsApp do Síndico") },
                    placeholder = { Text("(11) 99999-9999") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF25D366)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Text(
                    text = "Preview do Card / Mensagem:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = formattedMsg,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanPhone = selectedPhone.replace(Regex("[^0-9]"), "")
                            if (cleanPhone.isBlank()) {
                                Toast.makeText(context, "Informe o número de telefone do Síndico.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val phoneWithCountry = if (cleanPhone.length in 10..11 && !cleanPhone.startsWith("55")) {
                                "55$cleanPhone"
                            } else {
                                cleanPhone
                            }

                            val url = "https://api.whatsapp.com/send?phone=$phoneWithCountry&text=${Uri.encode(formattedMsg)}"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                onDismiss()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Enviar via WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun buildWhatsappOccurrenceMessage(
    occ: OccurrenceEntity,
    occNumber: Any,
    messages: List<MessageWithAttachments>
): String {
    val isCondo = occ.apartment == "CONDOMÍNIO"
    val unitStr = if (isCondo) "🏢 CONDOMÍNIO" else "Unidade ${occ.apartment}"
    val urgentStr = if (occ.isUrgent) " 🚨 *URGENTE*" else ""

    val lastMsgWithAtts = messages.lastOrNull { msgWithAtt ->
        val text = msgWithAtt.message.text.trim()
        text.isNotBlank() && !msgWithAtt.message.senderUsername.equals("SISTEMA", ignoreCase = true)
    } ?: messages.lastOrNull()

    val sb = StringBuilder()
    sb.append("📋 *ATENÇÃO PARA A OCORRÊNCIA L*\n")
    sb.append("-----------------------------------\n")
    sb.append("🔹 *Nº:* $occNumber\n")
    sb.append("🔹 *Título:* ${occ.title}\n")
    sb.append("🔹 *Local:* $unitStr\n")
    sb.append("🔹 *Status:* ${occ.status}$urgentStr\n")
    sb.append("🔹 *Data:* ${occ.date}\n")

    if (lastMsgWithAtts != null) {
        val msg = lastMsgWithAtts.message
        val sender = if (msg.senderUsername.isBlank()) "Anônimo" else msg.senderUsername
        val cleanMsgText = msg.text.trim()

        sb.append("\n💬 *Última mensagem (Atenção necessária):*\n")
        sb.append("\"$cleanMsgText\"\n")
        sb.append("✍️ _De: $sender | Data: ${msg.date}_\n")
    } else {
        sb.append("\n💬 *Mensagem / Observação:*\n")
        sb.append("_(Nenhuma mensagem registrada nesta ocorrência)_\n")
    }

    sb.append("\n⚠️ *Atenção Síndico:* Solicita-se a sua atenção para análise e providências referentes a esta ocorrência. (Responda no aplicativo)")

    return sb.toString()
}
