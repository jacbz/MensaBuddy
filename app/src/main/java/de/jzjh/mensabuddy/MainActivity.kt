package de.jzjh.mensabuddy

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.jzjh.mensabuddy.models.UserRecord
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var interval_start: Calendar = Calendar.getInstance()
    private var interval_end: Calendar = Calendar.getInstance()
    private var duration = 30

    private val spinnerItems = Array(10) { i -> (i + 3) * 5 }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        interval_start = Calendar.getInstance();
        if (interval_start.get(Calendar.HOUR_OF_DAY) < 12) {
            interval_start.set(Calendar.HOUR_OF_DAY, 12)
            interval_start.set(Calendar.MINUTE, 0)
        }
        interval_end = interval_start.clone() as Calendar
        interval_end.add(Calendar.MINUTE, 60)
        refreshDisplay()

        interval_start_textview.setOnClickListener { v ->
            picker(interval_start)
        }

        interval_end_textview.setOnClickListener { v ->
            picker(interval_end)
        }

        val spinnerAdapter = ArrayAdapter(this, R.layout.duration_spinner_item, spinnerItems)
        duration_spinner.adapter = spinnerAdapter
        duration_spinner.setSelection(3)
        duration_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                duration = spinnerItems[position]
            }
        }

        start_matching_button.setOnClickListener { v -> startMatching() }

        // auth in Firebase
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    //Snackbar.make(main_activity, "Auth success, UID " + user!!.uid, Snackbar.LENGTH_SHORT).show();

                    val userRecord = UserRecord(user!!.uid, Date())
                    db.collection("users")
                        .document(userRecord.uid)
                        .set(userRecord)
                        .addOnSuccessListener { documentReference ->
                        }
                        .addOnFailureListener { e ->
                        }

                } else {
                    Snackbar.make(main_activity, "Authentication failure!", Snackbar.LENGTH_SHORT)
                        .show();
                }
            }
    }

    fun refreshDisplay() {
        interval_start_textview.setText(Util.formatCal(interval_start))
        interval_end_textview.setText(Util.formatCal(interval_end))
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }

    fun startMatching() {
        val intent = Intent(this, MatchingActivity::class.java).apply {
            putExtra("interval_start_hour", interval_start.get(Calendar.HOUR_OF_DAY))
            putExtra("interval_start_minute", interval_start.get(Calendar.MINUTE))

            putExtra("interval_end_hour", interval_end.get(Calendar.HOUR_OF_DAY))
            putExtra("interval_end_minute", interval_end.get(Calendar.MINUTE))

            putExtra("min_duration", duration)
        }
        startActivity(intent)
    }

    fun picker(calendar: Calendar) {
        val timePicker: TimePickerDialog
        timePicker = TimePickerDialog(this,
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                refreshDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePicker.setTitle("Select interval")
        timePicker.show()
    }
}
