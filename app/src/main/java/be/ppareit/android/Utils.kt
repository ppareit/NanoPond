package be.ppareit.android

import android.content.res.Resources
import android.view.MenuItem
import android.view.View
import java.io.BufferedReader
import java.io.InputStreamReader

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
}

fun MenuItem.toggleChecked() {
    isChecked = !isChecked
}

fun Resources.openRawTextFile(id: Int): CharSequence {
    val result = StringBuilder()
    openRawResource(id).use { ins ->
        BufferedReader(InputStreamReader(ins)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
            }
        }
    }
    return result
}

fun sleepIgnoreInterrupt(millis: Long) {
    try {
        Thread.sleep(millis)
    } catch (ignored: InterruptedException) {
    }
}
