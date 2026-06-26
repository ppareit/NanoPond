package be.ppareit.nanopond.gui

import be.ppareit.nanopond.core.Cell
import kotlin.math.abs

internal object NanoPondColors {
    private val artificial = intArrayOf(
        0xffffffff.toInt(),
        0xff00ff00.toInt(),
        0xff00ffff.toInt(),
        0xffffff00.toInt(),
        0xffff0000.toInt(),
        0xffff00ff.toInt()
    )

    fun getColor(cell: Cell): Int {
        if (cell.lineage < 0) {
            val index = abs(cell.lineage).toInt() % artificial.size
            return artificial[index]
        }

        val lsp = cell.lineage.toInt()
        val alpha = 0xff
        val red = cap(lsp % 256)
        val green = cap(lsp % (256 * 256) / 256)
        val blue = cap(lsp % (256 * 256 * 256) / 256 / 256)
        return argb(alpha, red, green, blue)
    }

    private fun cap(i: Int): Int {
        return i.coerceIn(0, 255)
    }

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
