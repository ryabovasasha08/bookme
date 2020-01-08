package com.provectus_it.bookme.ui.screen.room_list

import android.annotation.SuppressLint
import com.provectus_it.bookme.Constants.ROOM_ID
import com.provectus_it.bookme.entity.Room
import com.provectus_it.bookme.repository.RoomRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class SelectedRoomManager (private val roomRepository: RoomRepository){

    private var defaultRoomIndex = 0
    private var roomList: List<Room>? = null

    var selectedRoomIndex: Int = defaultRoomIndex
        set(value) {
            field = value
            selectedRoomSubject.onNext(value)
        }

    private var selectedRoomSubject = BehaviorSubject.createDefault(defaultRoomIndex)

    init {
        subscribeOnRoomList()
    }

    @SuppressLint("CheckResult")
    private fun subscribeOnRoomList() {
        roomRepository.getRoomList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { if (it.isNotEmpty()) setDefaultRoomIndex(it) },
                        { Timber.e(it, "Failed to subscribe on currentDateTime changes") }
                )
    }

    fun setSelectedRoomIndexByRoomId(roomId: String) {
        selectedRoomIndex = roomList!!.indexOfFirst { it.id == roomId }
    }

    private fun setDefaultRoomIndex(rooms:List<Room>){
        roomList = rooms
        val newDefaultRoomIndex = rooms.indexOfFirst { it.id == ROOM_ID }
        if ((defaultRoomIndex != newDefaultRoomIndex) && (selectedRoomIndex == defaultRoomIndex)) selectedRoomIndex = newDefaultRoomIndex
        defaultRoomIndex = newDefaultRoomIndex
    }

    fun subscribeForSelectedRoom(): BehaviorSubject<Int> = selectedRoomSubject

    fun reset() {
        selectedRoomIndex = defaultRoomIndex
    }

}