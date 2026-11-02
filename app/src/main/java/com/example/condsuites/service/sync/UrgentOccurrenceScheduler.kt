package com.example.condsuites.service.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object UrgentOccurrenceScheduler {
    private const val PERIODIC_WORK_NAME = "urgent_occurrence_periodic_work"
    private const val ALARM_REQUEST_CODE = 99882
    const val INTERVAL_MILLIS = 15 * 60 * 1000L // 15 minutos

    fun scheduleUrgentAlerts(context: Context) {
        // 1. Agendar via WorkManager
        val constraints = Constraints.Builder().build()
        val workRequest = PeriodicWorkRequestBuilder<UrgentOccurrenceWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // 2. Agendar via AlarmManager para disparar mesmo com app totalmente fechado
        scheduleAlarm(context)
    }

    fun triggerImmediateCheck(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<UrgentOccurrenceWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun scheduleAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, UrgentOccurrenceAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MILLIS

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelUrgentAlerts(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, UrgentOccurrenceAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
