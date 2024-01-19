package com.example.aadhaarqr

import android.view.View
import android.widget.Button
import androidx.databinding.BindingAdapter

@BindingAdapter("showLayout")
fun Button.setVisibilityOfLayout(show: Boolean?) {
    show?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}