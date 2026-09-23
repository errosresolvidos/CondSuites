package com.example.condsuites.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
        val df = DecimalFormat("#,##0.00", symbols)
        val cleanString = text.text.replace(Regex("\\D"), "")
        if (cleanString.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        val parsed = cleanString.toDouble() / 100
        val formatted = df.format(parsed)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = text.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

class ProcessNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val cleanString = text.text.replace(Regex("\\D"), "").take(20)
        val out = StringBuilder()
        for (i in cleanString.indices) {
            out.append(cleanString[i])
            when (i) {
                6 -> out.append("-")
                8, 12, 13, 15 -> out.append(".")
            }
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 6) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                if (offset <= 13) return offset + 3
                if (offset <= 15) return offset + 4
                if (offset <= 20) return offset + 5
                return 25
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 16) return offset - 3
                if (offset <= 19) return offset - 4
                if (offset <= 25) return offset - 5
                return 20
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

fun calculateMonthlyDates(startDateStr: String, count: Int): List<String> {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dates = mutableListOf<String>()
    try {
        val startDate = sdf.parse(startDateStr) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        repeat(count) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }
    } catch (_: Exception) {
        val calendar = Calendar.getInstance()
        repeat(count) {
            dates.add(sdf.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }
    }
    return dates
}

fun formatCurrency(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(value)
}

fun calculateDaysDelay(dueDateStr: String): Long {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val dueDate = sdf.parse(dueDateStr) ?: return 0
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        if (today.after(dueDate)) TimeUnit.DAYS.convert(today.time - dueDate.time, TimeUnit.MILLISECONDS) else 0
    } catch (_: Exception) { 0 }
}

fun isMessageEditable(dateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: return false
        val diff = System.currentTimeMillis() - date.time
        diff < 5 * 60 * 1000 // 5 minutes
    } catch (_: Exception) {
        false
    }
}

fun isOvertimeEditable(registrationDateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return try {
        val date = sdf.parse(registrationDateStr) ?: return false
        val diff = System.currentTimeMillis() - date.time
        diff >= 0 && diff < 15 * 60 * 1000 // 15 minutos
    } catch (_: Exception) {
        false
    }
}

fun naturalSortApartments(apartment: String): String {
    return apartment.replace(Regex("(\\d+)")) { it.value.padStart(10, '0') }
}

fun calculateOvertimeHours(startStr: String, endStr: String): Double {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = sdf.parse(startStr) ?: return 0.0
        val end = sdf.parse(endStr) ?: return 0.0
        var diff = end.time - start.time
        if (diff < 0) diff += 24 * 60 * 60 * 1000
        val hours = diff.toDouble() / (1000 * 60 * 60)
        BigDecimal(hours).setScale(2, RoundingMode.HALF_UP).toDouble()
    } catch (_: Exception) {
        0.0
    }
}

fun getUnitIdForApartment(apt: String): Long {
    val clean = apt.trim()
    if (clean.isBlank()) return 1L
    val numOnly = clean.filter { it.isDigit() }.toLongOrNull()
    return if (numOnly != null && numOnly > 0) {
        numOnly
    } else {
        (clean.hashCode().toLong() and 0x7FFFFFFF)
    }
}
