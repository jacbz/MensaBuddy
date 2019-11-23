package de.jzjh.mensabuddy.models

import java.util.*

data class MatchingResultRecord(
    val match_time: Date,
    val uids: Array<String>,
    val interval_start_hour: Int,
    val interval_start_minute: Int,
    val interval_end_hour: Int,
    val interval_end_minute: Int
)