package com.georgcantor.wallpaperapp

import android.app.Application
import android.content.Context
import com.georgcantor.wallpaperapp.di.appModule
import com.georgcantor.wallpaperapp.di.repositoryModule
import com.georgcantor.wallpaperapp.di.viewModelModule
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        initializeSSLContext(this)

        startKoin {
            androidContext(this@MyApplication)
            modules(arrayListOf(appModule, viewModelModule, repositoryModule))
        }
    }

    companion object {
        @get:Synchronized
        var instance: MyApplication? = null
            private set
    }

    private fun initializeSSLContext(context: Context) {
        try {
            SSLContext.getInstance("TLSv1.2")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        try {
            ProviderInstaller.installIfNeeded(context.applicationContext)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

}
