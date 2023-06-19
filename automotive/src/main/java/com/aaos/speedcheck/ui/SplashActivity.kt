package com.aaos.speedcheck.ui

import android.Manifest
import android.car.Car
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aaos.speedcheck.R
import com.aaos.speedcheck.service.SpeedService

class SplashActivity : AppCompatActivity(), Animation.AnimationListener {

    object Constant {
        val permissions = arrayOf(
            Car.PERMISSION_SPEED,
            Car.PERMISSION_CAR_INFO,
            Manifest.permission.USE_FULL_SCREEN_INTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.top_anim)
        val logo = findViewById<ImageView>(R.id.logo)
        logo.startAnimation(logoAnimation)

        val textAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val icon = findViewById<TextView>(R.id.text)
        icon.startAnimation(textAnimation)

        logoAnimation?.setAnimationListener(this)
    }

    override fun onAnimationStart(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {
        var allGranted = true
        for(element in Constant.permissions){
            if(checkSelfPermission(element) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, Constant.permissions, 0)
                allGranted = false
                break
            }

        }
        if(allGranted) {
            val intent = Intent(this, SpeedService::class.java)
            startService(intent)
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var allGranted = true
        for(element in Constant.permissions){
            if(checkSelfPermission(element) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, Constant.permissions, 0)
                allGranted = false
                break
            }

        }
        if(allGranted) {
            val intent = Intent(this, SpeedService::class.java)
            startService(intent)
        }
    }

}