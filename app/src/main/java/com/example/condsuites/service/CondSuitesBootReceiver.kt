package com.example.condsuites.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.condsuites.service.sync.UrgentOccurrenceScheduler

class CondSuitesBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            CondSuitesBackgroundService.startService(context)
            UrgentOccurrenceScheduler.scheduleUrgentAlerts(context)
        }
    }
}
