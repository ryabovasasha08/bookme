package com.provectus_it.bookme.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.provectus_it.bookme.R
import kotlinx.android.synthetic.main.countdown_view.view.*

class CountdownView : FrameLayout {

    var text: String
        get() = countdownTextView.text.toString()
        set(value) {
            countdownTextView.text = value
        }

    var progress: Int
        get() = circleProgressBar.progress
        set(value) {
            circleProgressBar.progress = value
        }

    var max: Int
        get() = circleProgressBar.max
        set(value) {
            circleProgressBar.max = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        recycleViewsWithAttributes(context, attrs)
    }

    private fun init(context: Context) =
            LayoutInflater.from(context).inflate(R.layout.countdown_view, this, true)

    private fun recycleViewsWithAttributes(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CountdownView)
        circleProgressBar.progressDrawable = attributes.getDrawable(R.styleable.CountdownView_progressDrawable)
        circleProgressBar.background = attributes.getDrawable(R.styleable.CountdownView_background)
        countdownTextView.textSize = attributes.getDimension(R.styleable.CountdownView_textSize, DEFAULT_TEXT_SIZE)
        attributes.recycle()
    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 36f
    }

}