package com.example.core.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Formatters {
    private val localeId = Locale("id", "ID")
    private val wibZone = TimeZone.getTimeZone("Asia/Jakarta")

    fun formatRupiah(amount: Number?): String {
        if (amount == null) return "Rp 0"
        val symbols = DecimalFormatSymbols(localeId).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,###", symbols)
        return "Rp ${df.format(amount.toDouble())}"
    }

    fun formatRupiahCompact(amount: Number?): String {
        if (amount == null) return "Rp 0"
        val value = amount.toDouble()
        return when {
            value >= 1_000_000_000 -> {
                val formatted = String.format(localeId, "%.1f", value / 1_000_000_000).replace(".0", "")
                "Rp $formatted M"
            }
            value >= 1_000_000 -> {
                val formatted = String.format(localeId, "%.1f", value / 1_000_000).replace(".0", "")
                "Rp $formatted jt"
            }
            value >= 1_000 -> {
                val formatted = String.format(localeId, "%.0f", value / 1_000)
                "Rp $formatted rb"
            }
            else -> formatRupiah(amount)
        }
    }

    fun formatDateShort(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", localeId)
            val formatter = SimpleDateFormat("dd MMM yyyy", localeId)
            val d = parser.parse(dateStr)
            if (d != null) formatter.format(d) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatDateLong(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", localeId)
            val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId)
            val d = parser.parse(dateStr)
            if (d != null) formatter.format(d) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatIsoDateTime(isoStr: String?): String {
        if (isoStr.isNullOrBlank()) return "-"
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            )
            var parsedDate: Date? = null
            for (pattern in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(pattern, localeId)
                    parsedDate = sdf.parse(isoStr)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            if (parsedDate != null) {
                val out = SimpleDateFormat("dd MMM yyyy HH:mm", localeId).apply {
                    timeZone = wibZone
                }
                out.format(parsedDate)
            } else {
                isoStr
            }
        } catch (_: Exception) {
            isoStr
        }
    }

    fun formatTimeOnly(isoOrTimeStr: String?): String {
        if (isoOrTimeStr.isNullOrBlank()) return "-"
        if (isoOrTimeStr.length == 5 && isoOrTimeStr.contains(":")) {
            return isoOrTimeStr
        }
        return try {
            val inputFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            var parsedDate: Date? = null
            for (pattern in inputFormats) {
                try {
                    val sdf = SimpleDateFormat(pattern, localeId)
                    parsedDate = sdf.parse(isoOrTimeStr)
                    if (parsedDate != null) break
                } catch (_: Exception) {}
            }
            if (parsedDate != null) {
                val out = SimpleDateFormat("HH:mm", localeId).apply {
                    timeZone = wibZone
                }
                out.format(parsedDate)
            } else {
                isoOrTimeStr
            }
        } catch (_: Exception) {
            isoOrTimeStr
        }
    }

    fun formatDays(days: Number?): String {
        if (days == null) return "0 hari"
        val d = days.toDouble()
        val formatted = if (d % 1.0 == 0.0) {
            d.toInt().toString()
        } else {
            String.format(localeId, "%.1f", d)
        }
        return "$formatted hari"
    }

    fun formatHours(hours: Number?): String {
        if (hours == null) return "0 jam"
        val h = hours.toDouble()
        val formatted = if (h % 1.0 == 0.0) {
            h.toInt().toString()
        } else {
            String.format(localeId, "%.1f", h)
        }
        return "$formatted jam"
    }
}
