package com.sajdah.app.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.PrayerTimes
import java.util.Date

object AlarmScheduler {

    fun schedulePrayerAlarms(context: Context, prayerTimes: PrayerTimes) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val prayers = listOf(
            Pair("Fajr", prayerTimes.fajr),
            Pair("Dhuhr", prayerTimes.dhuhr),
            Pair("Asr", prayerTimes.asr),
            Pair("Maghrib", prayerTimes.maghrib),
            Pair("Isha", prayerTimes.isha)
        )

        for ((name, time) in prayers) {
            if (time.after(Date())) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", name)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    name.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            time.time,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            time.time,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Exact alarm permission not granted on Android 14+
                }
            }
        }
    }
}
