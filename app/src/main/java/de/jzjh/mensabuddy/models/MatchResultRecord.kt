package de.jzjh.mensabuddy.models

import java.util.*

data class MatchingResultRecord(
    val match_time: Date = Date(),
    val uids: List<String> = listOf(),
    val interval_start_hour: Int = 0,
    val interval_start_minute: Int = 0,
    val interval_end_hour: Int = 0,
    val interval_end_minute: Int = 0
)