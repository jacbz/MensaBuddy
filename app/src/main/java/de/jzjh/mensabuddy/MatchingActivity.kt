package de.jzjh.mensabuddy

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.jzjh.mensabuddy.models.MatchingRecord
import de.jzjh.mensabuddy.models.MatchingResultRecord
import kotlinx.android.synthetic.main.activity_matching.*
import java.util.*


class MatchingActivity : AppCompatActivity() {
    private var state: MatchingState = MatchingState.Connecting
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.uid!!
    lateinit var matchingRecord: MatchingRecord
    lateinit var matchingResultRecord: MatchingResultRecord

    var firebaseListener: ListenerRegistration? = null

    enum class MatchingState(val message: String) {
        Connecting("Connecting..."),
        Matching("Matching..."),
        MatchFound("Match found!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        cancel_button.setOnClickListener { v -> onBackPressed() }
        val interval_start_hour = intent.getIntExtra("interval_start_hour", 0)
        val interval_start_minute = intent.getIntExtra("interval_start_minute", 0)
        val interval_end_hour = intent.getIntExtra("interval_end_hour", 0)
        val interval_end_minute = intent.getIntExtra("interval_end_minute", 0)
        val min_duration = intent.getIntExtra("min_duration", 0)

        matchingRecord = MatchingRecord(uid, interval_start_hour, interval_start_minute,
            interval_end_hour, interval_end_minute, min_duration, Date()
        )

        renderState()

        db.collection("matching")
            .document(uid)
            .set(matchingRecord)
            .addOnSuccessListener { documentReference ->
                state = MatchingState.Matching
                renderState()

                listenForMatch()
            }
            .addOnFailureListener { e ->
            }
    }

    fun listenForMatch() {
        firebaseListener = db.collection("matchResults")
            .whereArrayContains("uids", uid)

            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("MatchingActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (value!!.size() > 0) {
                    val doc = value!!.first()
                    matchingResultRecord = doc.toObject(MatchingResultRecord::class.java)

                    // TODO: only 15 before, switch state

                    state = MatchingState.MatchFound
                    renderState()

                    animateMatchFound()
                }
            }
    }

    fun animateMatchFound() {
        val handler = Handler()

        loading_animation.animate().alpha(0f).duration = 1000

        handler.postDelayed({
            ObjectAnimator.ofFloat(matching_state_textview, "translationY", 400f).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 500)

        handler.postDelayed({
            match_found_animation.visibility = View.VISIBLE
            match_found_animation.playAnimation()

            matching_results_details.animate().alpha(1f).duration = 1000
        }, 1000)

    }

    fun renderState() {
        matching_state_textview.text = state.message

        val notificationTime = Util.calendarFromTime(
            matchingRecord.interval_start_hour,
            matchingRecord.interval_start_minute)
        notificationTime.add(Calendar.MINUTE, -15)

        if (state == MatchingState.Connecting) {
            matching_state_subtitle.visibility = View.GONE;
            matching_details.visibility = View.GONE;
            matching_results_details.alpha = 0f
        }
        else if (state == MatchingState.Matching) {
            matching_state_subtitle.visibility = View.VISIBLE;
            matching_details.visibility = View.VISIBLE;

            matching_state_subtitle.text = getString(R.string.notification_time, Util.formatCal(notificationTime))
            matching_details.text = getString(R.string.matching_parameters,
                Util.formatCal(matchingRecord.interval_start_hour, matchingRecord.interval_start_minute,
                    matchingRecord.interval_end_hour, matchingRecord.interval_end_minute),
                matchingRecord.min_duration)
        }
        else if (state == MatchingState.MatchFound) {
            if (firebaseListener != null) {
                firebaseListener!!.remove()
                firebaseListener = null
            }
            matching_state_subtitle.visibility = View.GONE;
            matching_details.visibility = View.GONE;
            matching_results_details.text = getString(R.string.matching_results,
                uid.substring(0, 3),
                matchingResultRecord.uids.find { x -> x != uid }!!.substring(0, 3),
                Util.formatCal(matchingResultRecord.interval_start_hour, matchingResultRecord.interval_start_minute))
        }
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
