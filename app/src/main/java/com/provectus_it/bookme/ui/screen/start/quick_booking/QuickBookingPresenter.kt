package com.provectus_it.bookme.ui.screen.start.quick_booking

import android.view.View
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.R
import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.ui.custom_view.DurationMaterialButton
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

@InjectViewState
class QuickBookingPresenter(
        private val eventRepository: EventRepository
) : MvpPresenter<QuickBookingView>() {

    private var roomName: String? = null

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun bookNow(duration: Duration) {
        eventRepository.bookEventNow(duration)
    }

    fun getRoomName(name: String) {
        roomName = name
    }

    fun setBookConfirmationDialogMessage(v: View) {
        val fromTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val toTime = LocalDateTime.from((v as DurationMaterialButton).duration.addTo(fromTime))
        val fromTimeString = dateTimeFormatter.format(fromTime)
        val toTimeString = dateTimeFormatter.format(toTime)
        val contextResources = v.context.resources
        val confirmationMessage = contextResources.getString(R.string.confirmation, roomName, fromTimeString, toTimeString)
        viewState.displayBookConfirmationDialog(v, confirmationMessage)

    }
}