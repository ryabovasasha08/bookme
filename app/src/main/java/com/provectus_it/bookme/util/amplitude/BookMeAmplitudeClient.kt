package com.provectus_it.bookme.util.amplitude

import android.annotation.SuppressLint
import com.amplitude.api.AmplitudeClient
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
object BookMeAmplitudeClient : AmplitudeClient() {

    private const val NAME_SESSION_ID = "session_id"

    override fun saveEvent(eventType: String?, event: JSONObject?): Long {
        try {
            event?.put(NAME_SESSION_ID, AmplitudeSessionsManager.currentSessionId)
        } catch (e: JSONException) {
            Timber.e(e, "Failed to replace session id.")
        }

        return super.saveEvent(eventType, event)
    }

    override fun logEvent(eventType:String, eventProperties:JSONObject?, apiProperties:JSONObject?,
    userProperties:JSONObject? , groups:JSONObject?, timestamp:Long, outOfSession:Boolean):Long {
        val result = super.logEvent(eventType, eventProperties, apiProperties, userProperties, groups, timestamp, outOfSession)
        eventProperties?.apply { Timber.d("$eventType: eventProperties = $eventProperties") }

        return result
    }

}