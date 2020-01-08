package com.provectus_it.bookme.ui.screen.room_info

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.provectus_it.bookme.ui.screen.event_list.EventListFragment
import org.threeten.bp.LocalDate
import kotlin.math.abs

class EventListPagerAdapter(fragment: Fragment, var roomId: String, var localDate: LocalDate) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment = EventListFragment.newInstance(roomId, calculateTime(position))

    override fun getItemCount(): Int = ITEM_COUNT

    fun calculateTime(position: Int): LocalDate {
        val positionDifference = abs(position - START_POSITION).toLong()

        return if (position > START_POSITION) {
            localDate.plusDays(positionDifference)
        } else {
            localDate.minusDays(positionDifference)
        }
    }

    companion object {
        const val ITEM_COUNT = Int.MAX_VALUE
        const val START_POSITION = ITEM_COUNT / 2
    }

}