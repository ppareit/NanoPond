/*******************************************************************************
 * Copyright (c) 2011 Pieter Pareit.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/
package be.ppareit.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * This class contains all logic related to running a game loop.<p>
 *
 * This class must be extended by your game. All game logic should happen in
 * onUpdate(). All drawing should happen in onDraw(). The game loop is started with
 * startGameLoop() and stopped with pauseGameLoop(). When the game loop is paused, and
 * the screen needs to be refreshed, call invalidate(). <p>
 *
 * While part of GameOfLife, this class can be reused in other applications that need
 * a view containing a game loop and the related logic.
 *
 */
public abstract class GameLoopView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = GameLoopView.class.getSimpleName();

    class AnimationThread extends Thread {

        private volatile boolean mRun;

        private long mLastTime = System.currentTimeMillis();

        private int mFrameSamplesCollected = 0;
        private int mFrameSampleTime = 0;
        private int mFps = 0;

        private final SurfaceHolder mSurfaceHolder;

        private final Paint mFpsTextPaint;

        public AnimationThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;

            mFpsTextPaint = new Paint();
            mFpsTextPaint.setARGB(255, 255, 0, 0);
            mFpsTextPaint.setTextSize(32);
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            Log.d(TAG, "AnimationThread.run'ing");

            // block until the surface is completely created in the main thread
            while (! surfaceCreatedCompleted) {
            }

            // run the gameloop
            while (mRun) {
                onUpdate();
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {
                        onDraw(canvas);
                        drawFps(canvas);
                    }
                } catch (Exception ignore) {
                } finally {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                sleepIfNeeded();
                updateFps();
            }
            Log.d(TAG, "AnimationThread.run'ed");
        }

        private long mNextGameTick = System.currentTimeMillis();

        /**
         * Will only sleep if we need to limit the frame rate to a certain number.
         */
        private void sleepIfNeeded() {
            if (mTargetFps <= 0) return;

            mNextGameTick += 1000 / mTargetFps;
            long sleepTime = mNextGameTick - System.currentTimeMillis();
            if (sleepTime >= 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.i("GameLoopView", "Failed to reach expected FPS!");
            }
        }

        public void setRunning(boolean state) {
            mRun = state;
        }

        private void updateFps() {
            long currentTime = System.currentTimeMillis();

            int timeDifference = (int)(currentTime - mLastTime);
            mFrameSampleTime += timeDifference;
            mFrameSamplesCollected++;

            if (mFrameSamplesCollected == 10) {
                mFps = ((10*1000) / mFrameSampleTime);

                mFrameSampleTime = 0;
                mFrameSamplesCollected = 0;
            }

            mLastTime = currentTime;
        }

        private void drawFps(Canvas canvas) {
            if (mDrawFps ==true && mFps != 0) {
                int x = getWidth() - getWidth() / 8;
                int y = getHeight() - (int)mFpsTextPaint.getTextSize() - 5;
                canvas.drawText(mFps + " fps", x, y, mFpsTextPaint);
            }
        }
    }

    private AnimationThread mThread = null;
    private int mTargetFps = 0;
    private boolean mDrawFps = true;

    public GameLoopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;

        getHolder().addCallback(this);

        setFocusable(true);
    }

    /**
     * Will start a seperate thread that runs the game loop. From within this will
     * call onUpdate() and onDraw().
     */
    public void startGameLoop() {
        Log.d(TAG, "startGameLoop'ing");
        // if thread exists, the gameloop is running
        if (mThread == null) {
            mThread = new AnimationThread(getHolder());
            mThread.setRunning(true);
            mThread.start();
        }
        Log.d(TAG, "startGameLoop'ed");
    }

    /**
     * Pauses the gameloop, this can be restared with startGameLoop().
     */
    public void pauseGameLoop() {
        // only pause a gameloop that is running
        if (mThread != null) {
            boolean retry = true;
            mThread.setRunning(false);
            while (retry) {
                try {
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // swallow exception and retry joining thread
                }
            }
            mThread = null;
        }
    }


    /**
     * Set's the frame rate at which the game loop should run. Be conservative and
     * implement an efficient onUpate()/onDraw() so this frame rate can be maintaned.
     *
     * @param fps The frame rate at which the game loop should run, set to zero to
     *              run as fast as possible.
     */
    public void setTargetFps(int fps) {
        mTargetFps = fps;
    }

    /**
     * If set to true, the gameloop will display the fps in the bottom right corner.
     *
     * @param show Flag indicating wheter to show the fps or not.
     */
    public void setDrawFps(boolean show) {
        mDrawFps  = show;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /* keeps track if the surface is completely created, the gameloop can only
     * run if we have a surface, so the gameloop thread has to block until so */
    private volatile boolean surfaceCreatedCompleted = false;

    @SuppressLint("WrongCall")
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isInEditMode()) return;
        Log.d(TAG, "surfaceCreated'ing");
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                onDraw(canvas);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
        surfaceCreatedCompleted = true;
        Log.d(TAG,"surfaceCreated'ed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @SuppressLint("WrongCall")
    @Override
    public void invalidate() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                onDraw(canvas);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Override this to implement the game logic.
     */
    abstract protected void onUpdate();

    /**
     * Override this to de the drawing.
     */
    @Override
    abstract protected void onDraw(Canvas canvas);

}
















