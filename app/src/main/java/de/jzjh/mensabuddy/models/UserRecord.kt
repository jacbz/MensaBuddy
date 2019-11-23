package de.jzjh.mensabuddy.models

import java.util.*

data class UserRecord(
    val uid: String = "",
    var lastSignIn: Date = Date(),
    val answers : Map<String, Int> = mapOf()) {

    fun updateSignInTime() {
        lastSignIn = Date()
    }
}