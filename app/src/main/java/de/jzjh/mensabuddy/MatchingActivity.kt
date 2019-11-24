package de.jzjh.mensabuddy

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
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
    var activityIsClosing = false

    var firebaseListener: ListenerRegistration? = null

    enum class MatchingState(val message: String) {
        Connecting("Connecting..."),
        Matching("Matching..."),
        MatchFound("Match found!"),
        LunchInProgress("Lunch in progress")
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

        message_button.setOnClickListener { view ->
            shareContactInfo(this)
        }
    }

    private fun shareContactInfo(context: Context) {
        val textInputLayout = TextInputLayout(context)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19), // if you look at android alert_dialog.xml, you will see the message textview have margin 14dp and padding 5dp. This is the reason why I use 19 here
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val input = EditText(context)
        textInputLayout.hint = "Your message"
        textInputLayout.addView(input)
        textInputLayout.isCounterEnabled = true
        textInputLayout.counterMaxLength = 128

        val alert = AlertDialog.Builder(context)
            .setTitle("Sharing contact information")
            .setView(textInputLayout)
            .setMessage("Input your contact information for your MensaBuddy if you would like to keep in touch. " +
                    "This information is only revealed to them if they also choose to share their contact details, and vice versa.")
            .setPositiveButton("Submit") { dialog, _ ->
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()
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
                } else {
                    // when the other party cancels at match, cancel too and close activity
                    if (!activityIsClosing && state == MatchingState.MatchFound) {
                        AlertDialog.Builder(this)
                            .setMessage("Sorry, your MensaBuddy cancelled the match :(\n" +
                                    "Please try again!")
                            .setCancelable(false)
                            .setPositiveButton("  :(  ") { dialog, id ->
                                deleteMatchingRecordAndQuit()
                            }
                            .show()
                    }
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
            match_found_animation.speed = 0.4f
            match_found_animation.playAnimation()

            matching_results_details.animate().alpha(1f).duration = 1000

        }, 1000)

        // transition into Lunch in Progress
        val transitionDelay = 600000 // 10 minutes
        val lunchTimeCal = Util.calendarFromTime(matchingResultRecord.interval_start_hour, matchingResultRecord.interval_start_minute)
        val timeUntilLunch = lunchTimeCal.time.time - Date().time
        handler.postDelayed({
            state = MatchingState.LunchInProgress
            renderState()
        }, Math.max(0, timeUntilLunch))

    }

    fun renderState() {
        Log.i("MA", "Current state: ${state}")
        matching_state_textview.text = state.message

        if (state == MatchingState.Connecting) {
            matching_state_subtitle.visibility = View.GONE;
            matching_details.visibility = View.GONE;
            matching_results_details.alpha = 0f
            message_button.hide()
        }
        else if (state == MatchingState.Matching) {
            matching_state_subtitle.visibility = View.VISIBLE;
            matching_details.visibility = View.VISIBLE;

//            val notificationTime = Util.calendarFromTime(
//                matchingRecord.interval_start_hour,
//                matchingRecord.interval_start_minute)
//            notificationTime.add(Calendar.MINUTE, -15)
//            matching_state_subtitle.text = getString(R.string.notification_time, Util.formatCal(notificationTime))
            matching_state_subtitle.text = getString(R.string.notification_time)
            matching_details.text = getString(R.string.matching_parameters,
                Util.formatCal(matchingRecord.interval_start_hour, matchingRecord.interval_start_minute,
                    matchingRecord.interval_end_hour, matchingRecord.interval_end_minute),
                matchingRecord.min_duration)
        }
        else if (state == MatchingState.MatchFound) {
            matching_state_subtitle.visibility = View.GONE;
            matching_details.visibility = View.GONE;
            matching_results_details.text = getString(R.string.matching_results,
                uid.substring(0, 3),
                matchingResultRecord.uids.find { x -> x != uid }!!.substring(0, 3),
                Util.formatCal(matchingResultRecord.interval_start_hour, matchingResultRecord.interval_start_minute))
        }
        else if (state == MatchingState.LunchInProgress) {
            matching_state_textview.text = MatchingState.LunchInProgress.message
            match_found_animation.speed = 2f
            match_found_animation.setAnimation(R.raw.lunch)
            message_button.show()

            ObjectAnimator.ofFloat(message_button, "translationX", 164f).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(cancel_button, "translationX", -164f).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(
                if (state == MatchingState.LunchInProgress) "Quit this screen?"
                else "Are you sure you want to cancel?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                deleteMatchingRecordAndQuit()
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun deleteMatchingRecordAndQuit() {
        activityIsClosing = true;
        db.collection("matching")
            .document(uid)
            .delete()
            .addOnSuccessListener { }
            .addOnFailureListener { }
        finish()
    }
}
