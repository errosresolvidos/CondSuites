$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$content = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)

$pattern = '(?s)val finalDesc = if \(isCondo && isElevatorTitle\) \{\s*val currentDate = SimpleDateFormat.*?\}\.toString\(\)\s*\} else desc'
$replacement = 'val finalDesc = if (isCondo && isElevatorTitle) {
                                    val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val payMethod = if (isInstallment) {
                                        val count = (installmentsCountStr.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                                        "Parcelado ($count x R$$ $${formatCurrency(totalVal / count)})"
                                    } else "À vista"

                                    StringBuilder().apply {
                                        append("📅 DATA: $$currentDate\n")
                                        append("📋 DESCRIÇÃO: $$desc\n")
                                        append("🏢 ANDAR: $$floor")
                                        if (currentUser.role != "Zelador" && valueStr.isNotBlank() && totalVal > 0) {
                                            append("\n💰 VALOR TOTAL: R$$ $${formatCurrency(totalVal)}\n")
                                            append("💳 PAGAMENTO: $$payMethod")
                                        }
                                    }.toString()
                                } else {
                                    StringBuilder().apply {
                                        append("📌 TÍTULO: $$title\n")
                                        append("🏢 LOCAL: $$finalApt\n")
                                        if (isUrgent) append("⚠️ PRIORIDADE: URGENTE\n")
                                        append("\n📋 DESCRIÇÃO:\n$$desc")
                                    }.toString()
                                }'

$content = [regex]::Replace($content, $pattern, $replacement)

# Also fix the garbled encoding in 'Nova OcorrÃªncia'
$content = $content.Replace('OcorrÃªncia', 'Ocorrência')

[System.IO.File]::WriteAllText($mainPath, $content, [System.Text.Encoding]::UTF8)
Write-Output "Success"
