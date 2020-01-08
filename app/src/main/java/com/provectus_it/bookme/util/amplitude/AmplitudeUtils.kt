package com.provectus_it.bookme.util.amplitude

import android.app.Application
import android.content.Context
import android.util.Log
import com.amplitude.api.Identify
import com.provectus_it.bookme.BuildConfig
import com.provectus_it.bookme.Constants.AMPLITUDE_API_KEY
import com.provectus_it.bookme.Constants.ROOM_NAME
import org.json.JSONObject

const val EVENT_USER_ADD_MEETING = "User_AddMeeting"
const val EVENT_USER_CHECKIN = "User_Checkin"
const val EVENT_USER_CHECKOUT = "User_Checkout"
const val EVENT_USER_HERE_BUTTON_TAP = "User_HereButtonTap"
const val EVENT_USER_QUICK_BOOK_TAP = "User_QuickBookTap"
const val EVENT_USER_VIEW_ADD_MEETING = "User_View_AddMeeting"
const val EVENT_USER_VIEW_ROOM_CONTAINER = "User_View_RoomContainer"
const val EVENT_USER_VIEW_DEFAULT_SCREEN = "User_View_DefaultScreen"
const val EVENT_USER_UNDO_ADD_MEETING = "User_UndoAddMeeting"
const val EVENT_USER_UNDO_CHECKOUT = "User_UndoCheckout"
const val PROPERTY_ACTION = "action"
const val PROPERTY_DURATION = "duration"
const val PROPERTY_FROM = "from"
const val PROPERTY_ROOM_ID = "room_id"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_TO = "to"
const val USER_PROPERTY_IS_TABLET = "is_tablet"
const val USER_PROPERTY_EVENT_COUNT = "event_count"

fun initAmplitude(context: Context) = BookMeAmplitudeClient.initialize(context, AMPLITUDE_API_KEY)
        .setUserId(ROOM_NAME)
        .setLogLevel(Log.DEBUG)
        .enableForegroundTracking(Application())
        .enableLogging(BuildConfig.DEBUG)!!

fun initIsTabletProperty() = identify(Identify().set(USER_PROPERTY_IS_TABLET, true))

fun logUserAddMeetingEvent(startTime: Long, endTime: Long, roomId: String) {
    identify(Identify().add(USER_PROPERTY_EVENT_COUNT, 1))

    val eventProperties = JSONObject()

    eventProperties.run {
        put(PROPERTY_FROM, startTime)
        put(PROPERTY_TO, endTime)
        put(PROPERTY_ROOM_ID, roomId)
    }

    sendLogToAmplitude(EVENT_USER_ADD_MEETING, eventProperties)
}

fun logUserHereButtonTapEvent() {
    sendLogToAmplitude(EVENT_USER_HERE_BUTTON_TAP)
}

fun logUserViewAddMeetingEvent(sourceScreen: AddMeetingSource) {
    val eventProperties = JSONObject().put(PROPERTY_SOURCE, sourceScreen.value)
    sendLogToAmplitude(EVENT_USER_VIEW_ADD_MEETING, eventProperties)
}

fun logUserViewRoomContainerEvent(action: ViewRoomContainerAction) {
    val eventProperties = JSONObject().put(PROPERTY_ACTION, action.value)
    sendLogToAmplitude(EVENT_USER_VIEW_ROOM_CONTAINER, eventProperties)
}

fun logUserViewDefaultScreenEvent(action: ViewRoomContainerAction) {
    val eventProperties = JSONObject().put(PROPERTY_ACTION, action.value)
    sendLogToAmplitude(EVENT_USER_VIEW_DEFAULT_SCREEN, eventProperties)
}

fun logUserUndoAddMeetingEvent(){
    sendLogToAmplitude(EVENT_USER_UNDO_ADD_MEETING)
}

fun logUserUndoCheckoutEvent(){
    sendLogToAmplitude(EVENT_USER_UNDO_CHECKOUT)
}

fun logUserCheckinEvent(){
    sendLogToAmplitude(EVENT_USER_CHECKIN)
}

fun logUserCheckoutEvent(){
    sendLogToAmplitude(EVENT_USER_CHECKOUT)
}

fun logUserQuickBookTapEvent(duration: Long) {
    val eventProperties = JSONObject().put(PROPERTY_DURATION, duration)
    sendLogToAmplitude(EVENT_USER_QUICK_BOOK_TAP, eventProperties)
}

private fun identify(identify: Identify) = BookMeAmplitudeClient.identify(identify, true)

private fun sendLogToAmplitude(eventName: String, eventProperties: JSONObject? = null) =
        when (eventProperties) {
            null -> BookMeAmplitudeClient.logEvent(eventName)
            else -> BookMeAmplitudeClient.logEvent(eventName, eventProperties)
        }

enum class AddMeetingSource(val value: String) {
    DEFAULT("default"),
    ROOM_INFO("room_info"),
    FREE_ITEM("free_item"),
    FREE_ITEM_DEFAULT("free_item_default")
}

enum class ViewRoomContainerAction(val value: String) { CLICK("click"), SWIPE("swipe") }