package com.berkedursunoglu.unutmadan.reminder

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.berkedursunoglu.unutmadan.R
import com.berkedursunoglu.unutmadan.databinding.ActivityReminderBinding
import com.berkedursunoglu.unutmadan.databinding.ReminderAlertdialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var dataBindingReminder: ReminderAlertdialogBinding
    private lateinit var dataBinding: ActivityReminderBinding
    private lateinit var viewModel: ReminderViewModel
    private lateinit var rv: ReminderRecyclerView
    private val cal = Calendar.getInstance()
    private lateinit var alarmManager: AlarmManager
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var dissmisAlert: AlertDialog
    private lateinit var guideShared: SharedPreferences


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_reminder)
        dataBindingReminder =
            DataBindingUtil.inflate(this.layoutInflater, R.layout.reminder_alertdialog, null, false)
        dataBinding.reminderRecyclerview.layoutManager =
            LinearLayoutManager(this.applicationContext)
        viewModel = ViewModelProvider(this)[ReminderViewModel::class.java]
        guideShared = this.getSharedPreferences("guideShared", MODE_PRIVATE)
        if (guideShared.getInt("guide",0) == 1){
            dataBinding.guideimage.visibility = View.GONE
        }
        recyclerView()
        dataBinding.addReminder.setOnClickListener {
            alertDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun alertDialog() {
        dataBindingReminder =
            DataBindingUtil.inflate(this.layoutInflater, R.layout.reminder_alertdialog, null, false)
        alertDialogsetTime()
        alertDialog = AlertDialog.Builder(this)
        alertDialog.setView(dataBindingReminder.root)
        dissmisAlert = alertDialog.create()
        dissmisAlert.show()
        dataBindingReminder.calenderButton.setOnClickListener {
            datePicker(it.context)
        }

        dataBindingReminder.clockButton.setOnClickListener {
            timerPicker(it.context)
        }

        dataBindingReminder.alarmbutton.setOnClickListener {
            if (checkTime(cal.timeInMillis)) {
                alarmManager()
                Toast.makeText(this.applicationContext, "Hatırlatma Kuruldu", Toast.LENGTH_LONG)
                    .show()
                dissmisAlert.dismiss()
            }
        }

    }

    private fun checkTime(setMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        val abs = setMillis - currentTime
        if (abs < 0) {
            Toast.makeText(this.applicationContext, R.string.timeralert, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun datePicker(context: Context) {
        val datePicker = DatePickerDialog.OnDateSetListener { _, i, i2, i3 ->
            cal.set(Calendar.YEAR, i)
            cal.set(Calendar.MONTH, i2)
            cal.set(Calendar.DAY_OF_MONTH, i3)
            val format = SimpleDateFormat("dd/MM/yyyy").format(cal.time)
            dataBindingReminder.reminderDatetextview.text = "Tarih: $format"
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, datePicker, year, month, day).show()

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun timerPicker(context: Context) {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val format = SimpleDateFormat("HH:mm").format(cal.time)
            dataBindingReminder.reminderTimetextview.text = "Saat: $format"
        }
        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun alertDialogsetTime() {
        val cal = Calendar.getInstance()
        cal.get(Calendar.YEAR)
        cal.get(Calendar.MONTH)
        cal.get(Calendar.DAY_OF_MONTH)
        val formatDate = SimpleDateFormat("dd/MM/yyyy").format(cal.time)
        dataBindingReminder.reminderDatetextview.text = "Tarih: $formatDate"
        cal.get(Calendar.HOUR_OF_DAY)
        cal.get(Calendar.MINUTE)
        val formatClock = SimpleDateFormat("HH:mm").format(cal.time)
        dataBindingReminder.reminderTimetextview.text = "Saat: $formatClock"
    }

    private fun sharedInteger(): Int {
        val shared = this.getSharedPreferences("uuidInt", MODE_PRIVATE)
        val edit = shared.edit()
        val int = shared.getInt("int", 0)
        val intplus = int + 1
        edit.putInt("int", intplus).apply()
        return int
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun alarmManager() {
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        }
        val requestCode = sharedInteger()
        val datetext = dataBindingReminder.reminderDatetextview.text.toString()
        val clocktext = dataBindingReminder.reminderTimetextview.text.toString()
        var desc = dataBindingReminder.reminderEdittext.text.toString()
        if (desc == "") {
            desc = "Hatırlatma"
        }
        val intent = Intent(this.applicationContext, AlarmReceiver::class.java).let {
            it.putExtra("desc", desc)
            it.putExtra("requestcode", requestCode)
            PendingIntent.getBroadcast(
                this.applicationContext,
                requestCode,
                it,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        }

        val timeMillis = cal.timeInMillis

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, intent)
        viewModel.insertDatabase(
            this.applicationContext,
            desc,
            clocktext,
            datetext,
            timeMillis,
            requestCode
        )
        guideShared()
        createNotificationChannel()
        recyclerView()
    }

    private fun recyclerView() {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.getAllDatabase(this@ReminderActivity)
            withContext(Dispatchers.Main) {
                viewModel.reminderArray.observe(this@ReminderActivity) {
                    rv = ReminderRecyclerView(it)
                    dataBinding.reminderRecyclerview.adapter = rv
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Reminder Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("reminderChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun guideShared(){
        guideShared = this.getSharedPreferences("guideShared", MODE_PRIVATE)
        val guidEdit = guideShared.edit()
        guidEdit.putInt("guide",1).apply()
    }
}