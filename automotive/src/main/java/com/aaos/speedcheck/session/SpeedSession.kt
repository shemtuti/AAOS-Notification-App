package com.aaos.speedcheck.session

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.aaos.speedcheck.ui.SpeedHomeScreen

// Session serves as the entry point for an app to display the initial screen on the app launch
// It has a lifecycle and can be considered similar to an activity in mobile android dev

// Responsible for returning the Screen instance to use the first time the app is started

class SpeedSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        return SpeedHomeScreen(carContext)
    }


}