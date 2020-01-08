package com.provectus_it.bookme.ui.screen.main.container

import com.provectus_it.bookme.R
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.ui.screen.room_info.RoomInfoFragment

class ContainerFragment : BaseFragment() {

    private var currentFragmentTag: String = ""

    override fun getLayoutResId(): Int = R.layout.fragment_container

    fun replace(roomId: String, roomName: String) {
        val newFragment = childFragmentManager.findFragmentByTag(roomId)
        val oldFragment = childFragmentManager.findFragmentByTag(currentFragmentTag)

        val fragmentTransaction = childFragmentManager.beginTransaction()

        if (newFragment == null) {
            fragmentTransaction.add(
                    R.id.fragmentContainer,
                    RoomInfoFragment.newInstance(roomId, roomName),
                    roomId
            )
        }

        if (oldFragment != null) {
            fragmentTransaction.hide(oldFragment)
            (oldFragment as RoomInfoFragment).notifyBeingShown()
        }

        if (newFragment != null) fragmentTransaction.show(newFragment)

        fragmentTransaction.commit()

        currentFragmentTag = roomId
    }
}
