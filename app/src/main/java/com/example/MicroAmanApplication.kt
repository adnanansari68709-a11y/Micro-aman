package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.MacroRepository
import com.example.engine.AutomationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MicroAmanApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MacroRepository(database.macroDao(), database.logDao(), database.variableDao()) }
    val automationEngine by lazy { AutomationEngine(this, repository) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()

        // Initialize default template macros if DB is empty
        applicationScope.launch {
            repository.populateDefaultTemplatesIfEmpty()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Micro Aman Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background macro monitor and trigger listener"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Micro Aman Automations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Macro notifications and alerts"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    companion object {
        const val CHANNEL_SERVICE_ID = "micro_aman_service_channel"
        const val CHANNEL_ALERTS_ID = "micro_aman_alerts_channel"

        lateinit var instance: MicroAmanApplication
            private set
    }
}
