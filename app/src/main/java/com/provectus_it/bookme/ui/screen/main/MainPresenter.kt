package com.provectus_it.bookme.ui.screen.main

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.preference.Preference_KioskModeValues
import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.ui.custom_view.FragmentViewPager.Companion.IS_MAIN_CONTAINER
import com.provectus_it.bookme.ui.screen.add_event.LastAddedEventManager
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_DEFAULT_VIEW
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_MAIN_CONTAINER
import com.provectus_it.bookme.ui.screen.room_info.CurrentDateModel
import com.provectus_it.bookme.ui.screen.room_list.SelectedRoomManager
import com.provectus_it.bookme.util.amplitude.AmplitudeSessionsManager
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction.CLICK
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction.SWIPE
import com.provectus_it.bookme.util.amplitude.logUserUndoAddMeetingEvent
import com.provectus_it.bookme.util.amplitude.logUserViewDefaultScreenEvent
import com.provectus_it.bookme.util.amplitude.logUserViewRoomContainerEvent
import com.provectus_it.bookme.util.ignore_update.IgnoreUpdateStatusManager
import com.provectus_it.bookme.util.kiosk_mode.KioskModeManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit

@InjectViewState
class MainPresenter(
        private val selectedRoomManager: SelectedRoomManager,
        private val mainViewPagerStateManager: MainViewPagerStateManager,
        private val currentDateModel: CurrentDateModel,
        private val kioskModePreference: Preference_KioskModeValues,
        private val kioskModeManager: KioskModeManager,
        private val eventRepository: EventRepository,
        private val context: Context,
        private val lastAddedEventManager: LastAddedEventManager,
        private val ignoreUpdateStatusManager: IgnoreUpdateStatusManager
) : MvpPresenter<MainView>() {

    private var checkoutEvent: Event? = null
    private var checkoutDateTime: LocalDateTime? = null

    private var activityRecreationDisposable: Disposable? = null
    private var sessionRecreationDisposable: Disposable? = null
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setCurrentPage(1, false)
        mainViewPagerStateManager.swipeToPosition = POSITION_DEFAULT_VIEW
        subscribeForAddedEvents()
    }

    fun notifyPageSelected(position: Int, isUserSwipe: Boolean, isUserAction: Boolean) {
        mainViewPagerStateManager.swipeToPosition = position

        when (position) {
            POSITION_MAIN_CONTAINER -> notifyMainContainerSelected(position, isUserSwipe, isUserAction)
            POSITION_DEFAULT_VIEW -> notifyDefaultScreenSelected(position, isUserSwipe, isUserAction)
            else -> throw IllegalArgumentException("Invalid number of page")
        }
    }

    fun notifyUserInteraction(currentPagerPosition: Int) {
        sessionRecreationDisposable?.dispose()
        sessionRecreationDisposable = sessionRecreatingObservable.subscribe()

        if (currentPagerPosition != POSITION_MAIN_CONTAINER) return

        activityRecreationDisposable?.dispose()
        activityRecreationDisposable = activityRecreatingObservable.subscribe()
    }

    fun notifyCountdownFinish() {
        viewState.recreateActivity()
        currentDateModel.reset()
        selectedRoomManager.reset()
    }

    fun notifyCountdownDialogDismiss() {
        viewState.deleteActivityBlur()
        notifyUserInteraction(POSITION_MAIN_CONTAINER)
    }

    fun notifyCheckoutSnackbarIsSelfDismissed(checkoutEvent: Event, checkoutDateTime: LocalDateTime) {
        eventRepository.checkoutBooking(checkoutDateTime, checkoutEvent)
    }

    private fun subscribeForAddedEvents() {
        val disposable = lastAddedEventManager.subscribeForAddedEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { viewState.showAddedEventCountdownSnackbar() },
                        { Timber.e(it, "Failed to subscribe on added events") }
                )

        compositeDisposable.add(disposable)
    }

    private fun notifyMainContainerSelected(position: Int, isUserSwipe: Boolean, isUserAction: Boolean) {
        if (isUserAction) logUserViewRoomContainerEvent(if (isUserSwipe) SWIPE else CLICK)
        IS_MAIN_CONTAINER = true
        notifyUserInteraction(position)
    }

    private fun notifyDefaultScreenSelected(position: Int, isUserSwipe: Boolean, isUserAction: Boolean) {
        activityRecreationDisposable?.dispose()
        sessionRecreationDisposable?.dispose()

        selectedRoomManager.reset()
        currentDateModel.reset()

        if (isUserAction) logUserViewDefaultScreenEvent(if (isUserSwipe) SWIPE else CLICK)
        IS_MAIN_CONTAINER = false
        notifyUserInteraction(position)
    }

    fun notifyUndoAddedEventButtonClick() {
        eventRepository.deleteEvent(
                lastAddedEventManager.lastAddedEventId,
                lastAddedEventManager.lastAddedEventEndTime
        )

        viewState.dismissCountdownSnackbar()

        logUserUndoAddMeetingEvent()
    }

    fun notifyUndoCheckoutButtonClick() {
        eventRepository.undoLocalCheckoutBooking(checkoutDateTime!!, checkoutEvent!!)
        viewState.dismissCountdownSnackbar()
    }

    fun notifyCheckoutMeeting(currentEvent: Event, currentDateTime: LocalDateTime) {
        checkoutEvent = currentEvent
        checkoutDateTime = currentDateTime
        viewState.showCheckoutCountdownSnackbar(currentEvent, currentDateTime)
        ignoreUpdateStatusManager.shouldIgnoreUpdateSubject.onNext(true)
    }

    fun setupKioskMode() {
        if (kioskModePreference.getIsFirstApplicationLaunch()) {
            if (kioskModePreference.getIsKioskModeEnabled()) {
                kioskModeManager.setKioskModeEnabled(context, true)
            }
        } else {
            kioskModeManager.setKioskModeEnabled(context, true)
            kioskModePreference.putIsFirstApplicationLaunch(true)
        }
    }

    fun notifyUIStateChanged(currentPageNumber: Int) {
        if (currentPageNumber == POSITION_MAIN_CONTAINER) viewState.hideNavigationPanel() else viewState.hideNavigationUI()
    }

    fun getCountDownSnackbarDuration() = COUNTDOWNSNACKBAR_DURATION_IN_MILLISECONDS

    override fun onDestroy() {
        super.onDestroy()

        activityRecreationDisposable?.dispose()
        sessionRecreationDisposable?.dispose()
        compositeDisposable.dispose()
    }

    private val activityRecreatingObservable = Observable.timer(ACTIVITY_RECREATING_DELAY_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                viewState.blurActivity()
                viewState.showCountdownDialog()
            }

    private val sessionRecreatingObservable = Observable.timer(NEW_SESSION_STARTING_DELAY_SECONDS, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { AmplitudeSessionsManager.startNewSession() }

    companion object {
        const val ACTIVITY_RECREATING_DELAY_SECONDS = 20L
        const val NEW_SESSION_STARTING_DELAY_SECONDS = 30L
        const val COUNTDOWNSNACKBAR_DURATION_IN_MILLISECONDS = 5000
    }

}