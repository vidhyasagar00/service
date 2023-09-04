package com.example.service

import android.annotation.SuppressLint
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var myService: MyService? = null
    private var serviceConnection: ServiceConnection? = null
    private var serviceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonStart: Button = findViewById(R.id.buttonThreadStarter)
        val buttonStop: Button = findViewById(R.id.buttonStopthread)

        buttonStart.setOnClickListener(this)
        buttonStop.setOnClickListener(this)

        serviceIntent = Intent(applicationContext, MyService::class.java)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonThreadStarter -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(serviceIntent)
                else
                    startService(serviceIntent)
            }
            R.id.buttonStopthread -> stopService(serviceIntent)
        }
    }

    override fun onDestroy() {
        myService = null
        serviceConnection = null
        serviceIntent = null
        super.onDestroy()
    }
}