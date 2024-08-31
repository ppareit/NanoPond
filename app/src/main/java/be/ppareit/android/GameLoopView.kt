package be.ppareit.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceView
import net.vrallev.android.cat.Cat

abstract class GameLoopView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    Runnable {

    private var isRunning = false
    private var gameThread: Thread? = null

    private var lastTime = System.currentTimeMillis()
    private var nextGameTick = System.currentTimeMillis()
    private var shouldDrawFps = true
    private var frameSampleTime = 0
    private var frameSamplesCollected = 0
    private var fps = 0
    private var targetFps = 0

    private var fpsTextPaint = Paint().apply {
        setARGB(255, 255, 0, 0)
        textSize = 32f
    }

    @SuppressLint("WrongCall")
    override fun run() {
        var canvas: Canvas? = null
        while (isRunning) {
            // always give the system some time
            sleepIgnoreInterrupt(1)
            try {
                if (holder.surface.isValid) {
                    canvas = holder.lockHardwareCanvas()
                    if (canvas != null) {
                        synchronized(holder) {
                            canvas.save()
                            onDraw(canvas)
                            drawFps(canvas)
                        }
                    }
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas)
            }
            sleepIfNeeded()
            updateFps()
        }
    }

    private fun updateFps() {
        val currentTime = System.currentTimeMillis()
        val timeDiff = (currentTime - lastTime).toInt()
        frameSampleTime += timeDiff
        frameSamplesCollected++
        if (frameSamplesCollected == 10) {
            fps = 10_000 / frameSampleTime
            frameSampleTime = 0
            frameSamplesCollected = 0
        }
        lastTime = currentTime
    }

    private fun drawFps(canvas: Canvas) {
        if (shouldDrawFps && fps != 0) {
            val x = (width - width / 8).toFloat();
            val y = height - fpsTextPaint.textSize - 5;
            canvas.drawText("$fps fps", x, y, fpsTextPaint);
        }
    }

    /**
     * Only sleep if we are limiting the fps
     */
    fun sleepIfNeeded() {
        if (targetFps <= 0)
            return // as we then run as fast as possible

        nextGameTick += 1000 / targetFps
        val sleepTime = nextGameTick - System.currentTimeMillis()
        if (sleepTime >= 0) {
            sleepIgnoreInterrupt(sleepTime)
        } else {
            Cat.i("Failed to reach expected FPS!")
        }
    }

    abstract fun onUpdate()

    fun setTargetFps(fps: Int) {
        targetFps = fps
    }

    fun startGameLoop() {
        isRunning = true
        gameThread = Thread(this)
        gameThread!!.start()
    }

    fun pauseGameLoop() {
        isRunning = false
        gameThread?.join()
    }

}