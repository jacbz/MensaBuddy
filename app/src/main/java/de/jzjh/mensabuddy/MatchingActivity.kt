package de.jzjh.mensabuddy

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.jzjh.mensabuddy.models.MatchingRecord
import kotlinx.android.synthetic.main.activity_matching.*
import java.util.*


class MatchingActivity : AppCompatActivity() {
    private var state: MatchingState = MatchingState.Connecting
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.uid!!

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

        val interval_start_hour = intent.getIntExtra("interval_start_hour", 0)
        val interval_start_minute = intent.getIntExtra("interval_start_minute", 0)
        val interval_end_hour = intent.getIntExtra("interval_end_hour", 0)
        val interval_end_minute = intent.getIntExtra("interval_end_minute", 0)
        val min_duration = intent.getIntExtra("min_duration", 0)


        val matchingRecord = MatchingRecord(uid, interval_start_hour, interval_start_minute,
            interval_end_hour, interval_end_minute, min_duration, Date()
        )

        db.collection("matching")
            .document(uid)
            .set(matchingRecord)
            .addOnSuccessListener { documentReference ->
                state = MatchingState.Matching
                displayState()

                listenForMatch()
            }
            .addOnFailureListener { e ->
            }
    }

    fun listenForMatch() {
        db.collection("matchResults")
            .whereArrayContains("uids", uid)
            .addSnapshotListener{ value, e ->
                if (e != null) {
                    Log.w("MatchingActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.size() > 0) {
                    state = MatchingState.MatchFound
                    displayState()
                }
            }
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
