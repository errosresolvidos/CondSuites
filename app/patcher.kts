import java.io.File

val mainPath = "src/main/java/com/example/condsuites/MainActivity.kt"
val file = File(mainPath)
var content = file.readText(Charsets.UTF_8)

val pattern = "(?s)val finalDesc = if \\(isCondo && isElevatorTitle\\) \\{.*?\\}\\.toString\\(\\)\\s*\\} else desc"

val replacement = """val finalDesc = if (isCondo && isElevatorTitle) {
                                    val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val payMethod = if (isInstallment) {
                                        val count = (installmentsCountStr.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                                        "Parcelado (${'$'}count x R${'$'} ${'$'}{formatCurrency(totalVal / count)})"
                                    } else "À vista"

                                    StringBuilder().apply {
                                        append("📅 DATA: ${'$'}currentDate\n")
                                        append("📋 DESCRIÇÃO: ${'$'}desc\n")
                                        append("🏢 ANDAR: ${'$'}floor")
                                        if (currentUser.role != "Zelador" && valueStr.isNotBlank() && totalVal > 0) {
                                            append("\n💰 VALOR TOTAL: R${'$'} ${'$'}{formatCurrency(totalVal)}\n")
                                            append("💳 PAGAMENTO: ${'$'}payMethod")
                                        }
                                    }.toString()
                                } else {
                                    StringBuilder().apply {
                                        append("📌 TÍTULO: ${'$'}title\n")
                                        append("🏢 LOCAL: ${'$'}finalApt\n")
                                        if (isUrgent) append("⚠️ PRIORIDADE: URGENTE\n")
                                        append("\n📋 DESCRIÇÃO:\n${'$'}desc")
                                    }.toString()
                                }"""

content = content.replace(Regex(pattern), java.util.regex.Matcher.quoteReplacement(replacement))
content = content.replace("Nova OcorrÃªncia", "Nova Ocorrência")

file.writeText(content, Charsets.UTF_8)
println("Success!")
