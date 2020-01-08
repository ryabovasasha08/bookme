package com.provectus_it.bookme.ui.screen.actual_statused_room

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.repository.RoomRepository
import com.provectus_it.bookme.util.update.MidnightUpdateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import timber.log.Timber

@InjectViewState
class ActualStatusedRoomPresenter(
        private val midnightUpdateManager: MidnightUpdateManager,
        private val roomRepository: RoomRepository
) : MvpPresenter<ActualStatusedRoomView>() {

    private val compositeDisposable = CompositeDisposable()
    private var getActualStatusedRoomDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        getActualStatusedRoom()
        subscribeForRefreshData()
    }

    private fun subscribeForRefreshData() {
        val disposable = midnightUpdateManager.subscribeForMidnightUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { getActualStatusedRoom() },
                        { Timber.e(it, "Failed to subscribe on refreshing actual statused room at midnight") }
                )

        compositeDisposable.add(disposable)
    }

    private fun getActualStatusedRoom() {
        getActualStatusedRoomDisposable?.dispose()

        getActualStatusedRoomDisposable = roomRepository.getActualStatusedRoom(LocalDateTime.now())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { viewState.onActualStatusedRoomUpdate(it) },
                        { Timber.e(it, "Failed to get actual room data") }
                )

        compositeDisposable.add(getActualStatusedRoomDisposable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}