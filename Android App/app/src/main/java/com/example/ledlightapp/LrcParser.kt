package com.example.ledlightapp

import java.util.regex.Pattern

data class LrcLine(
    val timestampMs: Long,
    val text: String
)

object LrcParser {
    private val lrcPattern = Pattern.compile("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\](.*)")

    fun parse(lrcText: String?): List<LrcLine> {
        if (lrcText.isNullOrBlank()) return emptyList()

        val lines = mutableListOf<LrcLine>()

//split line for each line song
        val rawLines = lrcText.split("\n")

        for (line in rawLines) {
            val matcher = lrcPattern.matcher(line.trim())
            if (matcher.matches()) {
                try {
                    val minutes = matcher.group(1)?.toLong() ?: 0L
                    val seconds = matcher.group(2)?.toLong() ?: 0L
                    val millisecondsGroup = matcher.group(3) ?: "00"

                    val milliseconds = if (millisecondsGroup.length == 2) {
                        millisecondsGroup.toLong() * 10
                    } else {
                        millisecondsGroup.toLong()
                    }

                    val text = matcher.group(4)?.trim() ?: ""
                    val totalMs = (minutes * 60 + seconds) * 1000 + milliseconds

                    lines.add(LrcLine(timestampMs = totalMs, text = text))
                } catch (e: Exception) {
                }
            }
        }

        return lines.sortedBy { it.timestampMs }
    }
}