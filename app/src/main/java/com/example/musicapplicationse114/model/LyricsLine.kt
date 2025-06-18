package com.example.musicapplicationse114.model

data class LyricLine(
    val timestampMs: Int, // milliseconds
    val text: String
)


fun parseLyrics(rawLyrics: String): List<LyricLine> {
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2})]([^\[]*)""")
    val result = mutableListOf<LyricLine>()

    for (match in regex.findAll(rawLyrics)) {
        val (min, sec, ms, text) = match.destructured
        val timestampMs = min.toInt() * 60_000 + sec.toInt() * 1000 + ms.toInt() * 10
        result.add(LyricLine(timestampMs, text.trim()))
    }

    return result.sortedBy { it.timestampMs }
}



fun getCurrentLyric(currentTimeMs: Int, lyrics: List<LyricLine>): String {
    for (i in lyrics.indices.reversed()) {
        if (currentTimeMs >= lyrics[i].timestampMs) {
            return lyrics[i].text
        }
    }
    return ""
}

