package com.provectus_it.bookme.util.amplitude

import com.provectus_it.bookme.util.toEpochMilliAtDefaultZone
import org.threeten.bp.LocalDateTime

object AmplitudeSessionsManager {

    var currentSessionId: Long = getCurrentTimestamp()
        private set

    fun startNewSession() {
        currentSessionId = getCurrentTimestamp()
    }

    private fun getCurrentTimestamp() = LocalDateTime.now().toEpochMilliAtDefaultZone()

}