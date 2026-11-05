package ru.urfu.cryptoapp.domain.model

import java.util.Locale

/** Время любимой пары. В профиле хранится как строка формата HH:mm. */
data class LectureTime(val hour: Int, val minute: Int) {

    fun format(): String = "%02d:%02d".format(Locale.US, hour, minute)

    companion object {
        private val FORMAT = Regex("([01]\\d|2[0-3]):([0-5]\\d)")

        /** null — строка не является временем в формате HH:mm. */
        fun parse(text: String): LectureTime? {
            val match = FORMAT.matchEntire(text.trim()) ?: return null
            return LectureTime(match.groupValues[1].toInt(), match.groupValues[2].toInt())
        }
    }
}
