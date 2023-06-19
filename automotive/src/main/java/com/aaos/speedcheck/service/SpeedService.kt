package com.aaos.speedcheck.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.notification.CarAppExtender
import androidx.car.app.validation.HostValidator
import androidx.core.app.NotificationCompat
import com.aaos.speedcheck.R
import com.aaos.speedcheck.session.SpeedSession
import com.aaos.speedcheck.ui.SpeedHomeScreen

// This class is the main bridge between our app and the host

class SpeedService : CarAppService() {
    private val channelId = "speeding_channel"
    private lateinit var car : Car
    private lateinit var mCarPropertyManager: CarPropertyManager

    companion object {
        private const val KM_MULTIPLIER = 3.59999987F // Use to convert miles to kms
        private const val notificationId = 1234
        private const val speedLimit = 140 // Speed limit in km/h
    }

    // Host validator - checks if the host that binds to our service cab be trusted and henceforth be used to provide session instances.
    override fun createHostValidator(): HostValidator {
        return if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE !=0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        }
        else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }
    }

    // Object that returns the first screen to be shown when the app is open
    // BUT in this case since the service primarily operates in the background and communicates with the user through notifications, there is no need for a dedicated UI screen.
        override fun onCreateSession(): Session {

        // No UI screen required for this service.Return an inner class SpeedingSession() instead of SpeedSession()
        return SpeedingSession()

        //return SpeedSession()
    }

    override fun onCreate() {
        super.onCreate()

        // initiate car
        initCar()

        // register car property callbacks
        registerCarPropertyCallback()

        // create notification channel
        createNotificationChannel()
    }

    private fun initCar() {
        car = Car.createCar(applicationContext)
        //car.connect()
        mCarPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    private fun registerCarPropertyCallback() {
        mCarPropertyManager.registerCallback(speedCallback,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_FASTEST
        )
    }

    private val speedCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
            val currentSpeed = ((carPropertyValue.value as Float) * KM_MULTIPLIER)

            if (currentSpeed > speedLimit) {
                showSpeedingNotification(currentSpeed)
            }
        }

        override fun onErrorEvent(i: Int, i1: Int) {
            Log.e(ContentValues.TAG, "CarPropertyManager Error!")
        }
    }

    // Create a notification object an
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Speeding Channel"
            val descriptionText = "Speeding notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250) // Vibration pattern for heads-up notification
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSpeedingNotification(currentSpeed: Float) {

        val notificationIntent = Intent(this, SpeedService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Speeding Alert!")
            .setContentText("Your current speed is $currentSpeed km/h")
            .setSmallIcon(R.drawable.app_icon)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
                // differentiate the notification style and content btn car ann phone (optional)
            .extend(
                CarAppExtender.Builder()
                    .setContentTitle("Car Speed Alert!")
                    .build()
            )
            .setFullScreenIntent(pendingIntent, true) // Show as heads-up notification

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification.build())

    }

    override fun onDestroy() {
        super.onDestroy()
        mCarPropertyManager.unregisterCallback(speedCallback)
        car.disconnect()
    }


    private inner class SpeedingSession : Session() {
        override fun onCreateScreen(intent: Intent): Screen {
            // No UI screen required for this service
            return object : Screen(carContext) {
                override fun onGetTemplate(): Template {

                    // Will not be displayed in this case scenario
                    val row = Row.Builder()
                        .setTitle("Monitoring Speed: ACTIVATED!")
                        .build()

                    val pane = Pane.Builder()
                        .addRow(row)
                        .build()

                    return PaneTemplate.Builder(pane)
                        .build()
                }
            }
        }
    }
}