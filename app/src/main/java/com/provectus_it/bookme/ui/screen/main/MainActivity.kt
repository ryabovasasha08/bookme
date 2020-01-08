package com.provectus_it.bookme.ui.screen.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.ui.activity.BaseActivity
import com.provectus_it.bookme.ui.custom_view.DurationSnackbar
import com.provectus_it.bookme.ui.custom_view.DurationSnackbar.Companion.make
import com.provectus_it.bookme.ui.screen.add_event.AddEventFragment
import com.provectus_it.bookme.util.amplitude.AddMeetingSource
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction
import com.provectus_it.bookme.util.amplitude.logUserViewAddMeetingEvent
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.get
import org.threeten.bp.LocalDateTime

class MainActivity : BaseActivity(), MainView, CountdownDialog.OnCountdownFinishListener {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter

    @ProvidePresenter
    fun provideMainPresenter() = get<MainPresenter>()

    private val mainPagerAdapter = MainPagerAdapter(supportFragmentManager)

    private var isUserSwipe = false
    private var isUserAction = true

    private var countdownSnackbar: DurationSnackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainPresenter.setupKioskMode()
        setContentView(R.layout.activity_main)
        mainViewPager.adapter = mainPagerAdapter
        mainViewPager.addOnPageChangeListener(onPageChangeListener)
    }

    fun openAddEventScreen(sourceScreen: AddMeetingSource, eventStartTime: LocalDateTime, eventEndTime: LocalDateTime, defaultRoomRemainingTime: Long, defaultRoomId: String) {
        logUserViewAddMeetingEvent(sourceScreen)
        supportFragmentManager
                .beginTransaction()
                .addToBackStack("AddEventFragment")
                .replace(android.R.id.content, AddEventFragment.newInstance(eventStartTime, eventEndTime, defaultRoomRemainingTime, defaultRoomId))
                .commit()
    }

    override fun openDevSettingsPanel() {
        devSettingsDrawer.openDrawer(GravityCompat.END)
    }

    override fun setCurrentPage(position: Int, smoothScroll: Boolean, action: ViewRoomContainerAction, isUserAction: Boolean) {
        this.isUserAction = isUserAction
        mainViewPager.setCurrentItem(position, smoothScroll)
    }

    private val onPageChangeListener: ViewPager.OnPageChangeListener =
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    notifyPageSelected(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                        isUserSwipe = true
                    }
                }
            }

    private val onUndoAddEventButtonClickListener =
            View.OnClickListener { mainPresenter.notifyUndoAddedEventButtonClick() }

    private val onUndoCheckoutButtonClickListener =
            View.OnClickListener { mainPresenter.notifyUndoCheckoutButtonClick() }

    private fun getCheckoutSnackBarCallBack(checkoutEvent: Event, checkoutDateTime: LocalDateTime): BaseTransientBottomBar.BaseCallback<DurationSnackbar?> {
        return object : BaseTransientBottomBar.BaseCallback<DurationSnackbar?>() {
            override fun onDismissed(transientBottomBar: DurationSnackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (transientBottomBar!!.isSelfDismissed) mainPresenter.notifyCheckoutSnackbarIsSelfDismissed(checkoutEvent, checkoutDateTime)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        mainPresenter.notifyUserInteraction(mainViewPager.currentItem)
    }

    override fun recreateActivity() {
        finish()

        val bundle = ActivityOptionsCompat.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        ).toBundle()

        startActivity(Intent(this, MainActivity::class.java), bundle)
    }

    override fun showCountdownDialog() {
        CountdownDialog(this).apply {
            setOnDismissListener { mainPresenter.notifyCountdownDialogDismiss() }
            setOnCountdownFinishListener(this@MainActivity)
            show()
        }
    }

    override fun blurActivity() {
        Blurry.with(this@MainActivity)
                .radius(ACTIVITY_BLUR_RADIUS)
                .onto(getContentView())
    }

    override fun deleteActivityBlur() {
        Blurry.delete(getContentView())
    }

    override fun onCountdownFinish() {
        mainPresenter.notifyCountdownFinish()
    }

    override fun hideNavigationPanel() {
        hideNavigationBar()
    }

    override fun hideNavigationUI() {
        hideSystemUi()
    }

    override fun showAddedEventCountdownSnackbar() {
        val duration = mainPresenter.getCountDownSnackbarDuration()
        val activityViewGroup = this.getContentView()
        countdownSnackbar = make(activityViewGroup, duration, R.string.meeting_added, onUndoAddEventButtonClickListener)
        (countdownSnackbar as DurationSnackbar).show()
    }

    override fun showCheckoutCountdownSnackbar(checkoutEvent: Event, checkoutDateTime: LocalDateTime) {
        val duration = mainPresenter.getCountDownSnackbarDuration()
        val activityViewGroup = this.getContentView()
        val checkoutSnackbarCallback = getCheckoutSnackBarCallBack(checkoutEvent, checkoutDateTime)
        countdownSnackbar = make(activityViewGroup, duration, R.string.checked_out_from_the_meeting, onUndoCheckoutButtonClickListener)
        (countdownSnackbar as DurationSnackbar).apply {
            addCallback(checkoutSnackbarCallback)
            show()
        }
    }

    fun notifyCheckoutMeeting(currentEvent: Event, currentDateTime: LocalDateTime) {
        mainPresenter.notifyCheckoutMeeting(currentEvent, currentDateTime)
    }

    override fun dismissCountdownSnackbar() {
        (countdownSnackbar as DurationSnackbar).apply {
            isSelfDismissed = false
            dismiss()
        }
    }

    private fun getContentView(): ViewGroup = findViewById<View>(android.R.id.content) as ViewGroup

    private fun notifyPageSelected(position: Int) {
        mainPresenter.apply {
            notifyPageSelected(position, isUserSwipe, isUserAction)
            mainPresenter.notifyUIStateChanged(mainViewPager.currentItem)
        }

        isUserSwipe = false
    }

    override fun onStart() {
        super.onStart()
        mainPresenter.notifyUIStateChanged(mainViewPager.currentItem)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mainPresenter.notifyUIStateChanged(mainViewPager.currentItem)
    }

    companion object {
        const val ACTIVITY_BLUR_RADIUS = 20
    }

}