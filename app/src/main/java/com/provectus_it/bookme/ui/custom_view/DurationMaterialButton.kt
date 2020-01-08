package com.provectus_it.bookme.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.provectus_it.bookme.Constants.MINUTES_IN_HOUR
import com.provectus_it.bookme.R
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.HOURS_FORMATTER
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.HOURS_MINUTES_FORMATTER
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.MINUTES_FORMATTER
import com.provectus_it.bookme.util.format
import org.threeten.bp.Duration

class DurationMaterialButton : MaterialButton {

    private var currentDuration: Duration = Duration.ZERO

    var duration: Duration
        get() = currentDuration
        set(value) {
            currentDuration = value
            setTextWithFormat(value)
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, R.attr.durationMaterialButtonStyle)

    private fun setTextWithFormat(duration: Duration) {
        text = when {
            duration.toHours() == 0L -> MINUTES_FORMATTER.format(duration)
            duration.toMinutes() == MINUTES_IN_HOUR.toLong() -> HOURS_FORMATTER.format(duration)
            else -> HOURS_MINUTES_FORMATTER.format(duration)
        }
    }

}