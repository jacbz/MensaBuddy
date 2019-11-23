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
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var interval_start: Calendar = Calendar.getInstance()
    private var interval_end: Calendar = Calendar.getInstance()
    private var duration = 30

    private val timeFormat = SimpleDateFormat("HH:mm")
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
        interval_end.add(Calendar.MINUTE, 30)


        interval_start_textview.setOnClickListener { v ->
            picker(interval_start, interval_start_textview)
        }

        interval_end_textview.setOnClickListener { v ->
            picker(interval_end, interval_end_textview)
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
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Snackbar.make(main_activity, "Auth success, UID " + user!!.uid, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(main_activity, "Authentication failure!", Snackbar.LENGTH_SHORT)
                        .show();
                }
            }
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

    fun picker(calendar: Calendar, textView: TextView) {
        val timePicker: TimePickerDialog
        timePicker = TimePickerDialog(this,
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                textView.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePicker.setTitle("Select interval")
        timePicker.show()
    }
}
