package com.provectus_it.bookme.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun ViewGroup.inflateChildView(layoutResId: Int): View {
    return LayoutInflater.from(context).inflate(layoutResId, this, false)
}