package com.provectus_it.bookme.ui.screen.room_info

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_DEFAULT_VIEW
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_MAIN_CONTAINER
import com.provectus_it.bookme.ui.screen.main.MainViewPagerStateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

@InjectViewState
class RoomInfoPresenter(
        private val mainViewPagerStateManager: MainViewPagerStateManager,
        private val currentDateModel: CurrentDateModel
) : MvpPresenter<RoomInfoView>() {

    private var currentDate: LocalDate? = null

    private val compositeDisposable = CompositeDisposable()

    private val dateFormatter = DateTimeFormatter.ofPattern("E, d MMMM")

    private var state = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeForCurrentDate()
        subscribeForMainViewPagerState()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun onActualStatusedRoomUpdate(statusedRoom: StatusedRoom) {
        changeBackSwipeButtonState(mainViewPagerStateManager.swipeToPosition)
        changeBackSwipeButtonColor(statusedRoom)

    }

    private fun subscribeForMainViewPagerState() {
        val disposable = mainViewPagerStateManager.subscribeForMainViewPagerState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { changeBackSwipeButtonState(it) },
                        { Timber.e(it, "Failed to subscribe on currentDateTime changes") }
                )

        compositeDisposable.add(disposable)
    }

    private fun changeBackSwipeButtonState(position: Int) {
        when (position) {
            POSITION_MAIN_CONTAINER -> viewState.showBackSwipeButton()
            POSITION_DEFAULT_VIEW -> viewState.hideBackSwipeButton()
            else -> throw IllegalArgumentException("Invalid number of page")
        }
    }

    private fun changeBackSwipeButtonColor(statusedRoom: StatusedRoom) {
        state = if (statusedRoom.availabilityInfo.isFree) FREE else BUSY
        if (mainViewPagerStateManager.swipeToPosition == POSITION_MAIN_CONTAINER) setStateColor()
    }

    fun setStateColor() = if (state == FREE) viewState.setFreeStateColor() else viewState.setBusyStateColor()

    fun handleCurrentDateTextClick() {
        viewState.showDatePickerDialog(
                currentDate!!.year,
                currentDate!!.monthValue - DATES_MONTH_DIFFERENCE,
                currentDate!!.dayOfMonth,
                MIN_DATE,
                MAX_DATE
        )
    }

    private fun subscribeForCurrentDate() {
        val disposable = currentDateModel.subscribeForCurrentDay()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onCurrentDateTimeUpdate(it) },
                        { Timber.e(it, "Failed to subscribe on currentDateTime changes") }
                )

        compositeDisposable.add(disposable)
    }

    fun handleDataChange(year: Int, month: Int, dayOfMonth: Int) {
        currentDateModel.currentDate =
                LocalDate.of(year, month + DATES_MONTH_DIFFERENCE, dayOfMonth)
    }

    fun notifyPageSelected(currentDate: LocalDate) {
        this.currentDate = currentDate
        currentDateModel.currentDate = currentDate
        viewState.setDate(currentDate.format(dateFormatter))
    }

    private fun isActiveRoom(
            currentDateTime: LocalDate?,
            receivedDateTime: LocalDate
    ): Boolean = currentDateTime == null || currentDateTime != receivedDateTime

    private fun onCurrentDateTimeUpdate(receivedDate: LocalDate) {
        if (!isActiveRoom(currentDate, receivedDate)) return

        currentDate = receivedDate
        viewState.setDate(currentDate!!.format(dateFormatter))
        viewState.setupAdapter(receivedDate)
    }

    companion object {
        private val MIN_DATE = Instant.EPOCH.toEpochMilli()
        private val MAX_DATE = Instant.ofEpochSecond(Integer.MAX_VALUE.toLong()).toEpochMilli()
        private const val FREE = 0
        private const val BUSY = 1
        private const val DATES_MONTH_DIFFERENCE = 1
    }

}