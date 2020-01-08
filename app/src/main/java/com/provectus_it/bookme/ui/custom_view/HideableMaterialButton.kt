package com.provectus_it.bookme.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.button.MaterialButton
import com.provectus_it.bookme.R
import com.provectus_it.bookme.util.behavior.HideableMaterialScrollingButtonBehavior

@CoordinatorLayout.DefaultBehavior(HideableMaterialScrollingButtonBehavior::class)
class HideableMaterialButton : MaterialButton {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs, R.attr.hideableButtonStyle)
}