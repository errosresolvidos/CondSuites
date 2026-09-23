import java.io.File
import java.util.regex.Pattern

val mainFile = File("src/main/java/com/example/condsuites/MainActivity.kt")
val artifactFile = File("../../AppData/Local/Google/AndroidStudio2026.1.3/projects/condsuites.400619ec/.artifacts/dc9f52c4-7dfb-4b9f-9259-89c4676038f4/arquivamento_ocorrencias.artifact.md")

val mainContent = mainFile.readText(Charsets.UTF_8)
val artifactContent = artifactFile.readText(Charsets.UTF_8)

val codeRegex = Regex("(?s)```kotlin\r?\n(.*?)```")
val match = codeRegex.find(artifactContent)
if (match != null) {
    val code = match.groupValues[1].trim()
    val pattern = "(?s)fun OccurrencesScreen\\(.*?(?=@Composable\\s*fun ProfessionalBudgetBadgeIcon\\()"
    val newMainContent = mainContent.replace(Regex(pattern)) {
        code + "\n\n"
    }
    mainFile.writeText(newMainContent, Charsets.UTF_8)
    println("Modificação do MainActivity feita com sucesso.")
} else {
    println("Código Kotlin não encontrado no artefato.")
}
