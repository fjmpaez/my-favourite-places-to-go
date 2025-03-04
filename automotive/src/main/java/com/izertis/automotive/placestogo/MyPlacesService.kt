package com.izertis.automotive.placestogo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.R
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.izertis.automotive.placestogo.screen.MainScreen


class MyPlacesService : CarAppService() {

    override fun onCreateSession() = MyPlacesSession()

    @SuppressLint("PrivateResource")
    override fun createHostValidator(): HostValidator {
        return if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(this)
                .addAllowedHosts(R.array.hosts_allowlist_sample)
                .build()
        }
    }
}

class MyPlacesSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainScreen(carContext)
    }
}
