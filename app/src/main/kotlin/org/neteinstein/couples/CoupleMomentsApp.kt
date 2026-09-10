package org.neteinstein.couples

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.neteinstein.couples.di.appModule

class CoupleMomentsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CoupleMomentsApp)
            modules(appModule)
        }
    }
}
