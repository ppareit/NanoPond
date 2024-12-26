/*******************************************************************************
 * Copyright (c) 2011 - 2018 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 * Contributors:
 * Pieter Pareit - initial API and implementation
 */

package be.ppareit.nanopond.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager
import be.ppareit.android.GameLoopView
import be.ppareit.nanopond.Cell
import be.ppareit.nanopond.NanoPond
import net.vrallev.android.cat.Cat
import kotlin.math.abs


private const val MIN_SCALE = 1.0f
private const val MAX_SCALE = 40.0f

class NanoPondView(context: Context, attrs: AttributeSet) : GameLoopView(context, attrs) {

    private var state = State.PAUSED

    private val nanoPond = (context as NanoPondActivity).nanoPond

    private val canvasPaint = Paint().apply { color = Color.GRAY }
    private val backgroundPaint = Paint().apply { color = Color.BLACK }
    private val cellPaint = Paint()
    private val activeCellPaint = Paint().apply { color = Color.CYAN }

    private val moveDetector = GestureDetector(context, MoveListener())
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private val drawMatrix = Matrix()

    // keep track of the active cell
    var activeCellRow = 0
        get() {
            check(field != -1) { "No cell active" }
            return field
        }
        private set

    val isCellActive: Boolean
        get() = activeCellCol != -1

    var activeCellCol: Int = 0
        get() {
            check(field != -1) { "No cell active" }
            return field
        }
        private set

    private val scale: Float
        get() = drawMatrix.mapRadius(1f)

    enum class State {
        RUNNING, PAUSED
    }

    init {
        setTargetFps(0)
    }

    fun setMode(mode: State) {
        if (mode == State.RUNNING && state != State.RUNNING) {
            startGameLoop()
            state = State.RUNNING
        } else if (mode == State.PAUSED && state != State.PAUSED) {
            pauseGameLoop()
            state = State.PAUSED
        }
    }

    override fun onUpdate() {
        // the nanopond object runs in its own thread thus that is updated continuously
    }

    private inner class MoveListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            drawMatrix.postTranslate(-distanceX, -distanceY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val pts = floatArrayOf(e.x, e.y)
            val inverse = Matrix()
            drawMatrix.invert(inverse)
            inverse.mapPoints(pts)
            val c = pts[0].toInt()
            val r = pts[1].toInt()
            Cat.d("Tapped: $c  $r")

            if (c in 0..NanoPond.POND_SIZE_X && r in 0..NanoPond.POND_SIZE_Y) {
                activeCellCol = c
                activeCellRow = r
            }
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor

            // limit zooming
            val scale = factor * scale
            if (scale !in MIN_SCALE..MAX_SCALE) {
                return false
            }

            val focusX = detector.focusX
            val focusY = detector.focusY
            drawMatrix.postScale(factor, factor, focusX, focusY)

            Cat.d("New scale: $scale")
            return true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        scaleDetector.onTouchEvent(event)
        moveDetector.onTouchEvent(event)
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (isInEditMode)
            return

        val pond = nanoPond.pond

        val cols = NanoPond.POND_SIZE_X
        val rows = NanoPond.POND_SIZE_Y
        val size = 1f

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), canvasPaint)

        canvas.save()
        canvas.concat(drawMatrix)

        canvas.drawRect(0f, 0f, cols * size, rows * size, backgroundPaint)

        // draw all the individual cells
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                val cell = pond[c][r]
                if (cell.generation > 2 && cell.energy > 0) {
                    val left = c * size
                    val top = r * size
                    val right = c * size + size
                    val bottom = r * size + size
                    cellPaint.color = getColor(cell)
                    canvas.drawRect(left, top, right, bottom, cellPaint)
                }
            }
        }

        // draw the active cell
        if (activeCellCol != -1) {
            var left = activeCellCol * size
            var top = activeCellRow * size
            var right = activeCellCol * size + size
            var bottom = activeCellRow * size + size
            canvas.drawRect(left, top, right, bottom, activeCellPaint)
            left += size / 8
            top += size / 8
            right -= size / 8
            bottom -= size / 8
            val cell = pond[activeCellCol][activeCellRow]
            if (cell.generation > 2 && cell.energy > 0) {
                cellPaint.color = getColor(cell)
                canvas.drawRect(left, top, right, bottom, cellPaint)
            } else {
                canvas.drawRect(left, top, right, bottom, backgroundPaint)
            }
        }

        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (isInEditMode)
            return

        Cat.d("size changed")

        drawMatrix.postTranslate(-NanoPond.POND_SIZE_X / 2f, -NanoPond.POND_SIZE_Y / 2f)

        // scale to display metrics
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)

        val scale = 4 * metrics.density
        drawMatrix.postScale(scale, scale)
        Cat.d("Size changed, new scale: $scale")

        // move left-top position to middle of screen
        drawMatrix.postTranslate(width / 2f, height / 2f)
    }

    companion object {
        private val artificial = intArrayOf(
            Color.WHITE, Color.GREEN, Color.CYAN, Color.YELLOW, Color.RED, Color.MAGENTA
        )

        private fun cap(i: Int): Int {
            return i.coerceIn(0, 255)
        }

        internal fun getColor(cell: Cell): Int {
            if (cell.lineage < 0){
                val index = abs(cell.lineage).toInt() % artificial.size
                return artificial[index]
            }

            val lsp = cell.lineage.toInt()
            val alpha = 0xff
            val red = cap(lsp % 256)
            val green = cap(lsp % (256 * 256) / 256)
            val blue = cap(lsp % (256 * 256 * 256) / 256 / 256)
            return Color.argb(alpha, red, green, blue)
        }
    }
}













