package com.provectus_it.bookme.ui.screen.main

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.provectus_it.bookme.R
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.MINUTES_SECONDS_FORMATTER
import com.provectus_it.bookme.util.amplitude.logUserHereButtonTapEvent
import com.provectus_it.bookme.util.format
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.countdown_dialog.*
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CountdownDialog(context: Context) : AlertDialog(context) {

    private var disposable: Disposable? = null

    private var onCountdownFinishListener: OnCountdownFinishListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countdown_dialog)
    }

    override fun onStart() {
        super.onStart()

        subscribeForCountdownTime()
        setupWindow()

        userPresenceMaterialButton.setOnClickListener {
            logUserHereButtonTapEvent()
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        disposable?.dispose()
    }

    private fun subscribeForCountdownTime() {
        val delayMillis = Duration.ofSeconds(DIALOG_CANCELLATION_DELAY_SECONDS).toMillis()

        countdown.max = delayMillis.toInt()
        disposable = startCountdown(delayMillis).subscribe(
                { changeTextOrCancelIfRequired(it) },
                { Timber.e(it, "Failed to change countdown state") }
        )
    }

    private fun startCountdown(delayMillis: Long): Observable<Long> {
        return Observable.interval(1, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(delayMillis)
                .map<Long> { delayMillis - it }
    }

    private fun changeTextOrCancelIfRequired(time: Long) {
        if (time == 1L) {
            dismiss()
            onCountdownFinishListener?.onCountdownFinish()
        }

        Duration.of(time, ChronoUnit.MILLIS).apply {
            countdown.text = MINUTES_SECONDS_FORMATTER.format(this.plusSeconds(1))
            countdown.progress = toMillis().toInt()
        }
    }

    private fun setupWindow() {
        window!!.apply {
            setBackgroundDrawableResource(android.R.drawable.screen_background_dark_transparent)
            setupAttribute(this)
        }
    }

    private fun getDimAmount(): Float {
        TypedValue().apply {
            context.resources.getValue(R.dimen.dim_amount, this, true)
            return this.float
        }
    }

    private fun setupAttribute(window: Window) {
        window.apply {
            attributes.apply {
                dimAmount = getDimAmount()
                height = ViewGroup.LayoutParams.MATCH_PARENT
                width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    fun setOnCountdownFinishListener(onCountdownFinishListener: OnCountdownFinishListener?) {
        this.onCountdownFinishListener = onCountdownFinishListener
    }

    companion object {
        const val DIALOG_CANCELLATION_DELAY_SECONDS = 10L
    }

    interface OnCountdownFinishListener {
        fun onCountdownFinish()
    }

}