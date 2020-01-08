package com.provectus_it.bookme

import android.app.Application
import com.akaita.java.rxjava2debug.RxJava2Debug
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.provectus_it.bookme.dev_settings_panel.developerSettingsModule
import com.provectus_it.bookme.di.*
import com.provectus_it.bookme.ui.screen.actual_statused_room.actualStatusedRoomModule
import com.provectus_it.bookme.ui.screen.add_event.addEventModule
import com.provectus_it.bookme.ui.screen.event_list.eventListModule
import com.provectus_it.bookme.ui.screen.main.mainModule
import com.provectus_it.bookme.ui.screen.room_info.roomInfoModule
import com.provectus_it.bookme.ui.screen.room_list.roomListModule
import com.provectus_it.bookme.ui.screen.start.startModule
import com.provectus_it.bookme.util.amplitude.initAmplitude
import com.provectus_it.bookme.util.amplitude.initIsTabletProperty
import com.provectus_it.bookme.util.ignore_update.ignoreUpdateModule
import com.provectus_it.bookme.util.logging.ErrorLoggingTree
import com.provectus_it.bookme.util.update.midnightUpdateModule
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class BookMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        initAmplitude(this)
        initIsTabletProperty()

        FirebaseApp.initializeApp(this)
        Fabric.with(this, Crashlytics())
        RxJava2Debug.enableRxJava2AssemblyTracking(arrayOf(applicationContext.packageName))

        if (BuildConfig.DEBUG) {
            initDebugToolsOnly()
        }

        startKoin {
            androidContext(this@BookMeApplication)
            modules(
                    listOf(
                            networkModule,
                            gsonModule,
                            repositoryModule,
                            databaseModule,
                            retrofitModule,
                            roomListModule,
                            roomInfoModule,
                            eventListModule,
                            startModule,
                            mainModule,
                            addEventModule,
                            actualStatusedRoomModule,
                            midnightUpdateModule,
                            ignoreUpdateModule,
                            preferenceModule,
                            kioskModeModule,
                            developerSettingsModule
                    )
            )
        }
    }

    private fun initStetho() {
        val initializerBuilder = Stetho.newInitializerBuilder(this)
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
        val initializer = initializerBuilder.build()
        Stetho.initialize(initializer)
    }

    private fun initDebugToolsOnly() {
        initStetho()
        Timber.plant(ErrorLoggingTree())
    }

}

