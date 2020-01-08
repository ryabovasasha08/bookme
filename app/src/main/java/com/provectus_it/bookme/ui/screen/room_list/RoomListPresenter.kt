package com.provectus_it.bookme.ui.screen.room_list

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.repository.RoomRepository
import com.provectus_it.bookme.util.update.MidnightUpdateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import timber.log.Timber

@InjectViewState
class RoomListPresenter(
        private val roomRepository: RoomRepository,
        private val midnightUpdateManager: MidnightUpdateManager,
        private val selectedRoomManager: SelectedRoomManager
) : MvpPresenter<RoomListView>() {

    private val compositeDisposable = CompositeDisposable()
    private var subscribeForRefreshEventsDisposable: Disposable? = null
    private var getActualStatusedRoomListDisposable: Disposable? = null

    private val roomAdapter: RoomAdapter = RoomAdapter()

    private var currentDateTime: LocalDateTime = LocalDateTime.now()

    private var roomList: List<StatusedRoom>? = null

    private var isFirstViewAttach: Boolean = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        roomAdapter.roomCallback = roomCallback
        viewState.setAdapter(roomAdapter)
        subscribeForRefreshEvents()
        getActualStatusedRoomList()
        subscribeForSelectedRoom()
    }

    override fun attachView(view: RoomListView?) {
        super.attachView(view)

        if (isFirstViewAttach) {
            isFirstViewAttach = false
        } else {
            roomRepository.updateRoomListIfRequired()
        }
    }

    private fun subscribeForSelectedRoom(){
        val disposable = selectedRoomManager.subscribeForSelectedRoom()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { selectItemIfRequired(it) },
                { Timber.e(it, "Failed to subscribe on changing selected room item") }
            )

        compositeDisposable.add(disposable)
    }

    private fun subscribeForRefreshEvents() {
        subscribeForRefreshEventsDisposable?.dispose()

        subscribeForRefreshEventsDisposable = midnightUpdateManager.subscribeForMidnightUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            currentDateTime = LocalDateTime.now()
                            getActualStatusedRoomList()
                        },
                        { Timber.e(it, "Failed to subscribe on refreshing events at midnight") }
                )

        compositeDisposable.add(subscribeForRefreshEventsDisposable!!)
    }

    private fun getActualStatusedRoomList() {
        getActualStatusedRoomListDisposable?.dispose()

        getActualStatusedRoomListDisposable = roomRepository.getStatusedRoomList(currentDateTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { setAdapterData(it) },
                        { Timber.e(it, "Failed to get statused room list") }
                )

        compositeDisposable.add(getActualStatusedRoomListDisposable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun setAdapterData(statusedRoomList: List<StatusedRoom>) {
        roomList = statusedRoomList
        roomAdapter.setData(statusedRoomList)
    }

    private fun selectItemIfRequired(selectedRoomIndex: Int){
        if (roomList != null) selectItem(roomList!!, selectedRoomIndex)
    }

    private fun selectItem(statusedRoomList:List<StatusedRoom>, newSelectedPosition: Int) {
        viewState.scrollToPosition(newSelectedPosition)
        val statusedRoom = statusedRoomList[newSelectedPosition]
        roomCallback.onRoomListItemClick(statusedRoom.room.id, statusedRoom.room.name, newSelectedPosition)
    }

    private val roomCallback: RoomCallback = object : RoomCallback {
        override fun onRoomListItemClick(roomId: String, roomName: String, newSelectedPosition: Int) {
            viewState.chooseRoom(roomId, roomName)
            roomAdapter.selectedPosition = newSelectedPosition
            if (selectedRoomManager.selectedRoomIndex != newSelectedPosition) selectedRoomManager.selectedRoomIndex = newSelectedPosition
        }
    }

}