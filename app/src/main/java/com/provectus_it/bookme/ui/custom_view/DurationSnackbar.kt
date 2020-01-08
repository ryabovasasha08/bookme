package com.provectus_it.bookme.ui.custom_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.provectus_it.bookme.R
import com.provectus_it.bookme.util.DateTimeFormatters
import com.provectus_it.bookme.util.format
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.duration_snackbar.view.*
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DurationSnackbar private constructor(
        @NonNull parent: ViewGroup,
        @NonNull content: View,
        private val snackbarText: Int
) : BaseTransientBottomBar<DurationSnackbar?>(parent, content, ContentViewCallback) {

    private var disposable: Disposable? = null

    var isSelfDismissed: Boolean = true

    override fun show() {
        super.show()

        view.snackbarMessageTextView.text = context.resources.getString(snackbarText)
        subscribeForCountdownTime(duration.toLong())
    }

    private fun subscribeForCountdownTime(delayMillis: Long) {
        view.snackbarCountdownView.max = delayMillis.toInt()
        disposable = startCountdown(delayMillis).subscribe(
                { changeTextOrDismissIfRequired(it) },
                { Timber.e(it, "Failed to change countdown state") }
        )
    }

    private fun changeTextOrDismissIfRequired(time: Long) {
        if (time == 1L) dismiss()

        org.threeten.bp.Duration.of(time, ChronoUnit.MILLIS).apply {
            view.snackbarCountdownView.text = DateTimeFormatters.SECONDS_FORMATTER.format(this.plusSeconds(1))
            view.snackbarCountdownView.progress = toMillis().toInt()
        }
    }

    private fun startCountdown(delayMillis: Long): Observable<Long> {
        return Observable.interval(1, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(delayMillis)
                .map { delayMillis - it }
    }

    private object ContentViewCallback : BaseTransientBottomBar.ContentViewCallback {

        override fun animateContentIn(delay: Int, duration: Int) {
            //no-op
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            //no-op
        }

    }

    companion object {
        fun make(parent: ViewGroup, duration: Int, snackbarText: Int, undoButtonClickListener: View.OnClickListener): DurationSnackbar {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.duration_snackbar, parent, false)
            val durationSnackbar = DurationSnackbar(parent, view, snackbarText)
            durationSnackbar.view.background = null
            durationSnackbar.view.undoMaterialButton.setOnClickListener(undoButtonClickListener)
            durationSnackbar.duration = duration

            return durationSnackbar
        }
    }

}