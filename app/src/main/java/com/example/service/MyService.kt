package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random


class MyService : Service() {
    var randomNumber = 0
        private set

    private var mIsRandomGeneratorOn = false

    private var job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var pendingPlayIntent: PendingIntent

    companion object {
        private const val SERVICE_DEMO_TAG = "service_demo_tag"
        const val MIN = 0
        const val MAX = 100
    }
   private lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        manager =  getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val id = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notificationChannelId else ""
        val playIntent = Intent(this, MyService::class.java)
        playIntent.action = "ACTION_PLAY"
        pendingPlayIntent = PendingIntent.getService(this, 0, playIntent,
            PendingIntent.FLAG_IMMUTABLE)
        val playAction =
            NotificationCompat.Action(android.R.drawable.ic_media_play, "stop1", pendingPlayIntent)

        val notification = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Random Number Generator")
            .addAction(playAction)
            .build()
        manager.notify(11,notification)
        startForeground(11, notification)
    }

    @get:RequiresApi(api = Build.VERSION_CODES.O)
    private val notificationChannelId: String
        get() {
            val channel = NotificationChannel(
                "myService", "RandomGenerator",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
            return channel.id
        }

    override fun onDestroy() {
        stopRandomNumberGenerator()
        Log.i(SERVICE_DEMO_TAG, "Service Destroyed")
        job.cancel()
        scope.cancel()
        Log.d(SERVICE_DEMO_TAG, "onDestroy: ${job.isActive}")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action != null && intent.action == "ACTION_PLAY") {
            Log.i(SERVICE_DEMO_TAG, "intent triggered")
            stopForeground(STOP_FOREGROUND_REMOVE)
//            stopService(Intent(applicationContext, MyService::class.java))
            stopSelfResult(startId)
        }
        Log.i(
            SERVICE_DEMO_TAG,
            "In onStartCommend, thread id: " + Thread.currentThread().id
        )
        mIsRandomGeneratorOn = true
        scope.launch {
            startRandomNumberGenerator()
        }
        return START_NOT_STICKY
    }

    private suspend fun startRandomNumberGenerator() {
        while (mIsRandomGeneratorOn) {
            try {
                delay(1000)
                if (mIsRandomGeneratorOn) {
                    randomNumber = Random().nextInt(MAX) + MIN
                    Log.i(
                        SERVICE_DEMO_TAG,
                        "Thread id: " + Thread.currentThread().id + ", Random Number: " + randomNumber
                    )
                }
            } catch (e: InterruptedException) {
                Log.i(SERVICE_DEMO_TAG, "Thread Interrupted")
            }
        }
    }

    private fun stopRandomNumberGenerator() {
        mIsRandomGeneratorOn = false
    }
}