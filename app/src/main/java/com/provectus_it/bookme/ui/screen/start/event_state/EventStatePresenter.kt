package com.provectus_it.bookme.ui.screen.start.event_state

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.Constants.DURATIONS
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.AvailabilityInfo
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.ui.screen.event_list.EventObject
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.HOURS_MINUTES_FORMATTER
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.MINUTES_SECONDS_FORMATTER
import com.provectus_it.bookme.util.amplitude.logUserCheckinEvent
import com.provectus_it.bookme.util.changeQuickBookingButtonsTime
import com.provectus_it.bookme.util.format
import com.provectus_it.bookme.util.isIndexInDurationsRange
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit

@InjectViewState
class EventStatePresenter : MvpPresenter<EventStateView>() {

    private var countdownDisposable: Disposable? = null

    private var currentEvent: EventObject? = null

    private var checkoutEvent: Event? = null

    private val compositeDisposable = CompositeDisposable()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onDestroy() {
        super.onDestroy()

        countdownDisposable?.dispose()
        compositeDisposable.dispose()
    }

    fun notifyCheckinCheckoutClick(checkinCheckoutText: CharSequence, context: Context) {
        if (isCheckinText(checkinCheckoutText, context)) {
            viewState.setCheckinCheckoutMaterialButtonText(R.string.check_out)
            checkoutEvent = (currentEvent as Event)
            logUserCheckinEvent()
        } else {
            viewState.notifyCheckoutBooking(LocalDateTime.now(), (currentEvent as Event))
        }
    }

    private fun isCheckinText(checkinCheckoutText: CharSequence, context: Context): Boolean {
        return checkinCheckoutText == context.resources.getString(R.string.check_in)
    }

    fun onActualStatusedRoomUpdate(actualStatusedRoom: StatusedRoom) {
        currentEvent = actualStatusedRoom.availabilityInfo.currentEvent
        changeDefaultScreenState(actualStatusedRoom)
        changeQuickBookButtonsVisibility(actualStatusedRoom.availabilityInfo)
        viewState.setRoomName(actualStatusedRoom.room.name)
    }

    private fun subscribeForCountdownTime(countdownDuration: AvailabilityInfo) {
        val countdownMaxValue = ChronoUnit.MILLIS.between(
                countdownDuration.currentEvent.startTime,
                countdownDuration.currentEvent.endTime
        )
        val remainingTime =
                ChronoUnit.MILLIS.between(LocalDateTime.now(), countdownDuration.timeUntil)

        viewState.setCountdownMax(countdownMaxValue.toInt())
        changeQuickBookButtonsVisibility(countdownDuration)
        countdownDisposable?.dispose()
        countdownDisposable = startCountdown(remainingTime)
                .subscribe(
                        { changeCountdownProgress(it) },
                        { Timber.e(it, "Failed to change countdown state") }
                )
    }

    private fun changeQuickBookButtonsVisibility(actualRoomInfo: AvailabilityInfo) {
        val remainingTime = ChronoUnit.MILLIS.between(LocalDateTime.now(), actualRoomInfo.timeUntil)

        if (actualRoomInfo.isFree) changeQuickBookButtonsState(remainingTime)
    }

    private fun changeCountdownProgress(time: Long) {
        Duration.of(time, ChronoUnit.MILLIS).apply {
            if (this.toHours() == 0L) {
                viewState.setCountdownText(MINUTES_SECONDS_FORMATTER.format(this))
            } else {
                viewState.setCountdownText(HOURS_MINUTES_FORMATTER.format(this))
            }

            viewState.setCountdownProgress(toMillis().toInt())
        }
    }

    private fun startCountdown(delayMillis: Long): Observable<Long> {
        return Observable.interval(1, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(delayMillis)
                .map { delayMillis - it }
    }

    private fun changeCheckinCheckoutMaterialButtonText(statusedRoom: StatusedRoom) {
        if (currentEvent != checkoutEvent) {
            viewState.setCheckinCheckoutMaterialButtonText(R.string.check_in)
        } else {
            viewState.setCheckinCheckoutMaterialButtonText(R.string.check_out)
        }
    }

    private fun changeDefaultScreenState(statusedRoom: StatusedRoom) {
        viewState.apply {
            setStateText(if (statusedRoom.availabilityInfo.isFree) R.string.free else R.string.busy)
            setStateTimeText(R.string.until, convertLocalDateTime(statusedRoom.availabilityInfo.timeUntil))
            setStateColor(if (statusedRoom.availabilityInfo.isFree) R.color.green_haze else R.color.my_sin)
            updateBackSwipeButtonState()
        }

        if (statusedRoom.availabilityInfo.isFree) {
            viewState.hideCheckinCheckoutMaterialButton()
        } else {
            viewState.showCheckinCheckoutMaterialButton()
            changeCheckinCheckoutMaterialButtonText(statusedRoom)
        }

        subscribeForCountdownTime(statusedRoom.availabilityInfo)
    }

    private fun changeQuickBookButtonsState(remainingTime: Long) {
        val index = DURATIONS.indexOfFirst { it.toMillis() <= remainingTime }
        val lastIndex = DURATIONS.lastIndex

        val quickButtonsTimeToBeSet = changeQuickBookingButtonsTime(remainingTime)
        viewState.setButtonDuration(quickButtonsTimeToBeSet.component1(), quickButtonsTimeToBeSet.component2(), quickButtonsTimeToBeSet.component3())

        if (!isIndexInDurationsRange(index)) {
            when (index) {
                lastIndex -> viewState.setButtonEnabled(firstIsEnabled = true)
                lastIndex - 1 -> viewState.setButtonEnabled(firstIsEnabled = true, secondIsEnabled = true)
                lastIndex - 2 -> viewState.setButtonEnabled(firstIsEnabled = true, secondIsEnabled = true, thirdIsEnabled = true)
                else -> viewState.setButtonEnabled(firstIsEnabled = false, secondIsEnabled = false, thirdIsEnabled = false)
            }
        } else {
            viewState.setButtonEnabled(firstIsEnabled = true, secondIsEnabled = true, thirdIsEnabled = true)
        }
    }

    private fun convertLocalDateTime(localDateTime: LocalDateTime): String = dateTimeFormatter.format(localDateTime)

}