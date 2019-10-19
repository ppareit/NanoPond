package be.ppareit.android

import android.view.MenuItem
import android.view.View

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
}

fun MenuItem.toggleChecked() {
    isChecked = !isChecked
}
