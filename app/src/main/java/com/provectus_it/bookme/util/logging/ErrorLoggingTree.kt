package com.provectus_it.bookme.util.logging

import android.util.Log
import com.akaita.java.rxjava2debug.RxJava2Debug
import com.crashlytics.android.Crashlytics
import com.provectus_it.bookme.BuildConfig
import org.jetbrains.annotations.NotNull
import timber.log.Timber


class ErrorLoggingTree : Timber.DebugTree() {

    private fun logHere(priority: Int, tag: String?, message: String, t: Throwable?) {
        val enhancedThrowable = if (t == null) t else RxJava2Debug.getEnhancedStackTrace(t)
        super.log(priority, tag, message, enhancedThrowable)
    }

    override fun log(priority: Int, tag: String?, @NotNull message: String, t: Throwable?) {
        if (BuildConfig.DEBUG) {
            logHere(priority, tag, message, t)
        }
        if (priority == Log.ERROR) {
            logToCrashlytics(message, t)
        }
    }

    private fun logToCrashlytics(message: String?, t: Throwable?) {
        if (message != null) {
            Crashlytics.log(message)
        }
        if (t != null) {
            Crashlytics.logException(t)
        }
    }

}