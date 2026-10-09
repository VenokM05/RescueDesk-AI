package com.rescuedesk.ai.app

import android.app.Application

class RescueDeskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
