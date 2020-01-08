package com.provectus_it.bookme.ui.screen.start

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.entity.Room
import com.provectus_it.bookme.repository.RoomRepository
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter
import com.provectus_it.bookme.ui.screen.main.MainViewPagerStateManager
import com.provectus_it.bookme.util.scheduleEveryMinuteUpdate
import com.provectus_it.bookme.util.update.MidnightUpdateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

@InjectViewState
class StartPresenter(
        private val midnightUpdateManager: MidnightUpdateManager,
        private val mainViewPagerStateManager: MainViewPagerStateManager,
        private val roomRepository: RoomRepository
) : MvpPresenter<StartView>() {

    private val compositeDisposable = CompositeDisposable()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("E, MMM d")

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        subscribeForMainViewPagerState()
        getActualRoomData()
        getActualTimeAndDate()
    }

    override fun attachView(view: StartView?) {
        super.attachView(view)
        viewState.scrollToCurrentEvent()
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

    fun updateBackSwipeButtonState() {
        changeBackSwipeButtonState(mainViewPagerStateManager.swipeToPosition)
    }

    private fun changeBackSwipeButtonState(position: Int) {
        when (position) {
            MainPagerAdapter.POSITION_MAIN_CONTAINER -> viewState.hideBackSwipeButton()
            MainPagerAdapter.POSITION_DEFAULT_VIEW -> notifyDefaultViewSelected()
            else -> throw IllegalArgumentException("Invalid number of page")
        }
    }

    private fun getActualRoomData() {
        val disposable = roomRepository.getActualRoomData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { setActualRoomData(it) },
                        { Timber.e(it, "Failed to get actual room data") }
                )

        compositeDisposable.add(disposable)
    }

    private fun getActualTimeAndDate() {
        val disposable = scheduleEveryMinuteUpdate()
                .map { LocalDateTime.now() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { setActualTimeAndDate(it) },
                        { Timber.e(it, "Failed to get actual time and date") }
                )

        compositeDisposable.add(disposable)
    }

    private fun notifyDefaultViewSelected() {
        viewState.apply {
            showBackSwipeButton()
            resetEventListState()
        }
    }

    private fun setActualRoomData(room: Room) {
        viewState.setRoomData(room)
        viewState.setScheduleHeader(room.name)
    }

    private fun setActualTimeAndDate(currentDateTime: LocalDateTime) {
        val currentTime = timeFormatter.format(currentDateTime)
        val currentDate = dateFormatter.format(currentDateTime)
        if (isMidnight(currentDateTime)) midnightUpdateManager.doOnMidnightUpdate()
        viewState.setTimeAndDate(currentTime, currentDate)
    }

    private fun isMidnight(currentDateTime: LocalDateTime): Boolean {
        return currentDateTime.truncatedTo(ChronoUnit.MINUTES).isEqual(LocalDate.now().atStartOfDay())
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}