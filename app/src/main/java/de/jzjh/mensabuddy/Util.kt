package de.jzjh.mensabuddy

import java.text.SimpleDateFormat
import java.util.*

class Util {
    companion object {

        val TimeFormat = SimpleDateFormat("HH:mm")

        fun formatCal(cal: Calendar): String {
            return TimeFormat.format(cal.time)
        }

        fun formatCal(hour: Int, minute: Int): String {
            return formatCal(calendarFromTime(hour, minute))
        }

        fun formatCal(hour1: Int, minute1: Int, hour2: Int, minute2: Int): String {
            return formatCal(hour1, minute1) + "-" +
                    formatCal(hour2, minute2)
        }

        fun calendarFromTime(hour: Int, minute: Int): Calendar {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            return cal
        }
    }
}