package de.jzjh.mensabuddy.models

import java.util.*

data class MatchingRecord(
    val uid: String,
    val interval_start_hour: Int,
    val interval_start_minute: Int,
    val interval_end_hour: Int,
    val interval_end_minute: Int,
    val min_duration: Int,
    val update_time: Date
)