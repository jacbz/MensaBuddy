package de.jzjh.mensabuddy

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_matching.*


class MatchingActivity : AppCompatActivity() {
    private val state: MatchingState = MatchingState.Connecting

    enum class MatchingState(val message: String) {
        Connecting("Connecting..."),
        Matching("Matching..."),
        MatchFound("Match found!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        cancel_button.setOnClickListener { v -> onBackPressed() }

        displayState()

        val db = FirebaseFirestore.getInstance()
    }

    fun displayState() {
        matching_state_textview.text = state.message
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to cancel?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id -> super@MatchingActivity.onBackPressed() }
            .setNegativeButton("No", null)
            .show()
    }
}
