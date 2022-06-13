package com.berkedursunoglu.unutmadan.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val desc = p1?.getStringExtra("desc")
        var intent = Intent(p0?.applicationContext, ReminderService::class.java)
        intent.putExtra("desc", desc)
        val requestcode = p1?.getIntExtra("requestcode",0)
        intent.putExtra("requestcode",requestcode)
        p0?.applicationContext?.startService(intent)
    }

}