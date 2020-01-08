package com.provectus_it.bookme.util.behavior

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton

class HideableMaterialScrollingButtonBehavior(context: Context?, attrs: AttributeSet?) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var currentAnimationState = ANIM_STATE_NONE

    private var currentHideableButton: View? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return super.layoutDependsOn(parent, child, dependency) || dependency is MaterialButton
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(
                coordinatorLayout,
                child,
                directTargetChild,
                target,
                axes
        )
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int
    ) {
        super.onNestedScroll(
                coordinatorLayout,
                child,
                target,
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed
        )

        currentHideableButton = coordinatorLayout.getDependencies(child).first { it is MaterialButton }.also {
            if (!it.isEnabled) return

            if (dyConsumed > 0) hideButton(it) else if (dyConsumed < 0) showCurrentHideableButton(it)
        }
    }

    private fun showCurrentHideableButton(hideableButton: View) {
        hideableButton.apply {
            if (isOrWillBeShown(this)) return

            animate().cancel()

            if (shouldAnimateVisibilityChange(this)) {
                showButtonWithAnimation(this)
            } else {
                visibility = View.VISIBLE
            }
        }
    }

    fun reset(hideableButton: View) {
        showButtonWithAnimation(hideableButton)
        hideableButton.visibility = View.VISIBLE
    }

    private fun hideButton(hideableButton: View) {
        hideableButton.apply {
            if (isOrWillBeHidden(this)) return

            animate().cancel()

            if (shouldAnimateVisibilityChange(this)) {
                currentAnimationState = ANIM_STATE_HIDING
                animate().translationY((-height).toFloat())
                        .setDuration(SHOW_HIDE_ANIM_DURATION.toLong())
                        .setListener(hideAnimatorListenerAdapter)
            }
        }
    }

    private fun isOrWillBeShown(mView: View): Boolean {
        return if (mView.visibility != View.VISIBLE) {
            currentAnimationState == ANIM_STATE_SHOWING
        } else {
            currentAnimationState != ANIM_STATE_HIDING
        }
    }

    private fun shouldAnimateVisibilityChange(hideableButton: View): Boolean {
        return ViewCompat.isLaidOut(hideableButton) && !hideableButton.isInEditMode
    }

    private fun showButtonWithAnimation(hideableButton: View) {
        currentAnimationState = ANIM_STATE_SHOWING
        hideableButton.animate().translationY(HIDEABLE_BUTTON_PADDING)
                .setDuration(SHOW_HIDE_ANIM_DURATION.toLong())
                .setListener(showAnimatorListenerAdapter)
    }

    private fun isOrWillBeHidden(hideableButton: View): Boolean {
        return if (hideableButton.visibility == View.VISIBLE) {
            currentAnimationState == ANIM_STATE_HIDING
        } else {
            currentAnimationState != ANIM_STATE_SHOWING
        }
    }

    private val showAnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            currentHideableButton?.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animator?) {
            currentAnimationState = ANIM_STATE_NONE
        }
    }

    private val hideAnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        private var isCancelled = false

        override fun onAnimationStart(animation: Animator?) {
            currentHideableButton?.visibility = View.VISIBLE
            isCancelled = false
        }

        override fun onAnimationCancel(animation: Animator?) {
            isCancelled = true
        }

        override fun onAnimationEnd(animation: Animator?) {
            if (!isCancelled) currentHideableButton?.visibility = View.GONE
            currentAnimationState = ANIM_STATE_NONE
        }
    }

    companion object {
        private const val ANIM_STATE_NONE = 0
        private const val ANIM_STATE_HIDING = 1
        private const val ANIM_STATE_SHOWING = 2
        const val SHOW_HIDE_ANIM_DURATION = 200
        const val HIDEABLE_BUTTON_PADDING = 6f
    }
}