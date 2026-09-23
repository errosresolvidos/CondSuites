import java.io.File
import java.util.regex.Pattern

val mainFile = File("src/main/java/com/example/condsuites/MainActivity.kt")
val compFile = File("src/main/java/com/example/condsuites/OccurrenceComponents.kt")

var content = mainFile.readText(Charsets.UTF_8)

// 1. Get imports
val importsRegex = Regex("(?s)^package.*?import.*?(?=@Entity|@Composable|class|fun|val|var)")
val importsMatch = importsRegex.find(content)
val imports = importsMatch?.value ?: ""

// 2. Extract OccurrencesScreen
val regex1 = Regex("(?s)(@OptIn\\(ExperimentalMaterial3Api::class\\)\\s*@Composable\\s*fun OccurrencesScreen\\(.*?)\\s*(?=@OptIn|@Composable|fun ProfessionalBudgetBadgeIcon)")
val match1 = regex1.find(content)
val occScreen = match1?.groupValues?.get(1)

// 3. Extract OccurrenceCard
val regex2 = Regex("(?s)(@OptIn\\(ExperimentalFoundationApi::class\\)\\s*@Composable\\s*fun OccurrenceCard\\(.*?)\\s*(?=@OptIn|@Composable|fun RegisterOccurrenceDialog)")
val match2 = regex2.find(content)
val occCard = match2?.groupValues?.get(1)

// 4. Extract RegisterOccurrenceDialog
val regex3 = Regex("(?s)(@OptIn\\(ExperimentalMaterial3Api::class\\)\\s*@Composable\\s*fun RegisterOccurrenceDialog\\(.*?)\\s*(?=@OptIn|@Composable|fun OccurrenceLogsDialog)")
val match3 = regex3.find(content)
val regDialog = match3?.groupValues?.get(1)

if (occScreen != null && occCard != null && regDialog != null) {
    println("Found all three functions!")
    content = content.replace(occScreen, "")
    content = content.replace(occCard, "")
    content = content.replace(regDialog, "")
    
    mainFile.writeText(content, Charsets.UTF_8)
    
    val newCompContent = imports + "\n\n" + occScreen + "\n\n" + occCard + "\n\n" + regDialog + "\n"
    compFile.writeText(newCompContent, Charsets.UTF_8)
    println("Extraction complete.")
} else {
    println("Missing functions. OccScreen: ${occScreen != null}, OccCard: ${occCard != null}, RegDialog: ${regDialog != null}")
}
