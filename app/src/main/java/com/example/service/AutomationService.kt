package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MicroAmanApplication
import com.example.engine.SensorTriggerManager
import com.example.model.TriggerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class AutomationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var sensorTriggerManager: SensorTriggerManager? = null
    private var lastBatteryLevel = -1
    private var isRegistered = false

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val engine = (application as? MicroAmanApplication)?.automationEngine ?: return

            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    engine.onTriggerFired(
                        TriggerType.POWER_CONNECTED,
                        mapOf("is_charging" to "true")
                    )
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    engine.onTriggerFired(
                        TriggerType.POWER_DISCONNECTED,
                        mapOf("is_charging" to "false")
                    )
                }
                Intent.ACTION_SCREEN_ON -> {
                    engine.onTriggerFired(TriggerType.SCREEN_ON)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    engine.onTriggerFired(TriggerType.SCREEN_OFF)
                }
                Intent.ACTION_USER_PRESENT -> {
                    engine.onTriggerFired(TriggerType.DEVICE_UNLOCKED)
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) {
                        engine.onTriggerFired(TriggerType.HEADPHONES_PLUGGED)
                    } else if (state == 0) {
                        engine.onTriggerFired(TriggerType.HEADPHONES_UNPLUGGED)
                    }
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    if (level >= 0 && scale > 0) {
                        val batteryPct = (level * 100 / scale)
                        if (batteryPct != lastBatteryLevel) {
                            lastBatteryLevel = batteryPct
                            engine.onTriggerFired(
                                TriggerType.BATTERY_LEVEL,
                                mapOf(
                                    "level" to batteryPct.toString(),
                                    "is_charging" to isCharging.toString()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        val app = application as? MicroAmanApplication
        val engine = app?.automationEngine

        // Register Dynamic Hardware Broadcasts
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(eventReceiver, filter)
        isRegistered = true

        // Accelerometer sensor trigger for Shake & Flip
        sensorTriggerManager = SensorTriggerManager(
            context = this,
            onShakeDetected = {
                engine?.onTriggerFired(TriggerType.SHAKE_DEVICE)
            },
            onFlipFaceDown = {
                engine?.onTriggerFired(TriggerType.FLIP_DEVICE)
            }
        )
        sensorTriggerManager?.startListening()

        // Time of Day / Interval monitor loop
        startTimeMonitoringLoop()
    }

    private fun startTimeMonitoringLoop() {
        serviceScope.launch {
            var lastCheckedMinute = -1
            while (isActive) {
                val now = Calendar.getInstance()
                val currentMinute = now.get(Calendar.MINUTE)
                val currentHour = now.get(Calendar.HOUR_OF_DAY)

                if (currentMinute != lastCheckedMinute) {
                    lastCheckedMinute = currentMinute
                    val engine = (application as? MicroAmanApplication)?.automationEngine
                    engine?.onTriggerFired(
                        TriggerType.TIME_OF_DAY,
                        mapOf(
                            "hour" to currentHour.toString(),
                            "minute" to currentMinute.toString()
                        )
                    )
                }
                delay(20000) // Check every 20 seconds
            }
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MicroAmanApplication.CHANNEL_SERVICE_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Micro Aman Engine Active")
            .setContentText("Monitoring battery, device events, and sensor triggers in background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (isRegistered) {
            try {
                unregisterReceiver(eventReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        sensorTriggerManager?.stopListening()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, AutomationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AutomationService::class.java)
            context.stopService(intent)
        }
    }
}
