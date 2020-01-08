package com.provectus_it.bookme.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.provectus_it.bookme.entity.Room
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface RoomDao : BaseDao<Room> {

    @Query("SELECT * FROM room ORDER BY name")
    fun subscribe(): Flowable<List<Room>>

    @Query("SELECT name FROM room WHERE id = :roomId")
    fun getName(roomId: String): Single<String>

    @Query("DELETE FROM room")
    fun delete()

    @Transaction
    fun deleteAndInsert(roomList:List<Room>) {
        delete()
        insert(roomList)
    }
}