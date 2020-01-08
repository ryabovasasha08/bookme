package com.provectus_it.bookme.ui.screen.room_list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.provectus_it.bookme.R
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.ui.screen.main.container.ContainerFragment
import kotlinx.android.synthetic.main.fragment_room_list.*
import org.koin.android.ext.android.get

class RoomListFragment : BaseFragment(), RoomListView {

    @InjectPresenter
    lateinit var roomListPresenter: RoomListPresenter

    @ProvidePresenter
    fun provideRoomListPresenter() = get<RoomListPresenter>()

    private var appToolbarElevation: Float? = null

    override fun getLayoutResId(): Int = R.layout.fragment_room_list

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appToolbarElevation = context.resources.getDimension(R.dimen.app_toolbar_elevation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomListRecyclerView.layoutManager = LinearLayoutManager(context)
        roomListRecyclerView.addOnScrollListener(onScrollListener)
    }

    override fun setAdapter(roomListAdapter: RecyclerView.Adapter<*>) {
        roomListRecyclerView.adapter = roomListAdapter
    }

    override fun chooseRoom(roomId: String, roomName: String) {
        (parentFragment as ContainerFragment).replace(roomId, roomName)
    }

    override fun scrollToPosition(position:Int){
        (roomListRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(position)
    }

    private val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (recyclerView.canScrollVertically(-1)) {
                appBarLayout.elevation = appToolbarElevation!!
            } else {
                appBarLayout.elevation = 0f
            }
        }
    }

}
