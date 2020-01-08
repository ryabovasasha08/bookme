package com.provectus_it.bookme.ui.screen.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.provectus_it.bookme.ui.screen.main.container.ContainerFragment
import com.provectus_it.bookme.ui.screen.start.StartFragment

class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = when (position) {
        POSITION_MAIN_CONTAINER -> ContainerFragment()
        POSITION_DEFAULT_VIEW -> StartFragment()
        else -> throw IllegalArgumentException("Invalid number of page")
    }

    override fun getCount() = ITEM_COUNT

    companion object {
        const val ITEM_COUNT = 2
        const val POSITION_MAIN_CONTAINER = 0
        const val POSITION_DEFAULT_VIEW = 1
    }

}