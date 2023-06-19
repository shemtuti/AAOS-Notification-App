package com.aaos.speedcheck.ui

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class SpeedHomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val actionStrip = createActionSetup()

        val row = Row.Builder()
            .setTitle("Monitoring Speed: ACTIVATED!")
            .build()

        val pane = Pane.Builder()
            .addRow(row)
            .build()

        return PaneTemplate.Builder(pane)
            .setActionStrip(actionStrip)
            .build()

    }

    private fun createActionSetup(): ActionStrip {
        val dismissAction = Action.Builder()
            .setIcon(CarIcon.APP_ICON)
            .build()

        return ActionStrip.Builder()
            .addAction(dismissAction)
            .build()
    }
}