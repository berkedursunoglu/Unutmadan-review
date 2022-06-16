package com.berkedursunoglu.unutmadan.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.berkedursunoglu.unutmadan.services.ReminderDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(p0: Context?, p1: Intent?) {
        if ("android.intent.action.BOOT_COMPLETED" == p1?.action) {
            alarmManager(p0!!.applicationContext)
        } else {
            val desc = p1?.getStringExtra("desc")
            var intent = Intent(p0?.applicationContext, ReminderService::class.java)
            intent.putExtra("desc", desc)
            val requestcode = p1?.getIntExtra("requestcode", 0)
            intent.putExtra("requestcode", requestcode)
            p0?.applicationContext?.startService(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun alarmManager(context: Context) {
        var alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        }
        GlobalScope.launch(Dispatchers.IO) {
            var arraylist = ReminderDatabase.invoke(context).reminderDao().getAllReminder()
            withContext(Dispatchers.Main) {
                for (i in arraylist) {
                    val intent = Intent(context, AlarmReceiver::class.java).let {
                        it.putExtra("desc", i.descReminder)
                        it.putExtra("requestcode", i.requestCode)
                        PendingIntent.getBroadcast(
                            context,
                            i.requestCode,
                            it,
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        i.clockTimeMillis,
                        intent
                    )
                }
            }

        }

    }
}