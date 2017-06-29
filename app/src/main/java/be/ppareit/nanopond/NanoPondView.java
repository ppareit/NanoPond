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
package be.ppareit.nanopond;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;

import net.vrallev.android.cat.Cat;

import be.ppareit.android.GameLoopView;

/**
 *
 */
public class NanoPondView extends GameLoopView {

    private static final float MIN_SCALE =  1.0f;
    private static final float MAX_SCALE = 40.0f;

    public enum State {
        RUNNING, PAUSED,
    }

    private State mState = State.PAUSED;

    private NanoPond mNanoPond = null;

    private Paint mCanvasPaint = null;
    private Paint mBackgroundPaint = null;
    private final Paint mCellPaint = new Paint();
    private final Paint mActiveCellPaint = new Paint();

    // Useful paint to display debugging info on screen
    private final Paint mDebugPaint = new Paint();

    private GestureDetector mMoveDetector;
    private ScaleGestureDetector mScaleDetector;

    private Matrix mDrawMatrix = new Matrix();

    // keep track of the active cell
    private int mActiveCellCol = 0;
    private int mActiveCellRow = 0;

    public NanoPondView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode())
            return;

        mNanoPond = ((NanoPondActivity) context).getNanoPond();

        mCanvasPaint = new Paint();
        mCanvasPaint.setColor(Color.GRAY);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);

        mDebugPaint.setARGB(255, 255, 240, 0);
        mDebugPaint.setTextSize(32);

        mActiveCellPaint.setColor(Color.CYAN);

        setTargetFps(0);

        mMoveDetector = new GestureDetector(context, new MoveListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setMode(State mode) {
        if (mode == State.RUNNING && mState != State.RUNNING) {
            startGameLoop();
            mState = State.RUNNING;
        } else if (mode == State.PAUSED && mState != State.PAUSED) {
            pauseGameLoop();
            mState = State.PAUSED;
        }
    }

    public boolean isCellActive() {
        return (mActiveCellCol != -1);
    }

    public int getActiveCellCol() {
        if (mActiveCellCol == -1)
            throw new IllegalStateException("No cell active");
        return mActiveCellCol;
    }

    public int getActiveCellRow() {
        if (mActiveCellRow == -1)
            throw new IllegalStateException("No cell active");
        return mActiveCellRow;
    }

    @Override
    protected void onUpdate() {
        // the nanopond object runs in its own thread thus that is updated continuously
    }

    private class MoveListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mDrawMatrix.postTranslate(-distanceX, -distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float[] pts = { e.getX(), e.getY() };
            Matrix inverse = new Matrix();
            mDrawMatrix.invert(inverse);
            inverse.mapPoints(pts);
            final int c = (int) pts[0];
            final int r = (int) pts[1];
            Cat.d("Tapped: " + c + "  " + r);

            if (0 <= c && c < NanoPond.POND_SIZE_X && 0 <= r && r < NanoPond.POND_SIZE_Y) {
                mActiveCellCol = c;
                mActiveCellRow = r;
            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();

            // limit zooming
            final float scale = factor * getScale();
            if (scale < MIN_SCALE || MAX_SCALE < scale) {
                return false;
            }

            final float focusX = detector.getFocusX();
            final float focusY = detector.getFocusY();
            mDrawMatrix.postScale(factor, factor, focusX, focusY);

            Cat.d("New scale: " + getScale());
            return true;
        }
    }

    private float getScale() {
        return mDrawMatrix.mapRadius(1.f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode())
            return;

        final Cell[][] pond = mNanoPond.pond;

        final int cols = NanoPond.POND_SIZE_X;
        final int rows = NanoPond.POND_SIZE_Y;
        final float size = 1;

        canvas.drawRect(0, 0, getWidth(), getHeight(), mCanvasPaint);

        canvas.save();
        canvas.concat(mDrawMatrix);

        canvas.drawRect(0, 0, cols * size, rows * size, mBackgroundPaint);

        // draw all the individual cells
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                final Cell cell = pond[c][r];
                if (cell.generation > 2 && cell.energy > 0) {
                    float left = (c * size);
                    float top = (r * size);
                    float right = (c * size + size);
                    float bottom = (r * size + size);
                    mCellPaint.setColor(getColor(cell));
                    canvas.drawRect(left, top, right, bottom, mCellPaint);
                }
            }
        }

        // draw the active cell
        if (mActiveCellCol != -1) {
            float left = (mActiveCellCol * size);
            float top = (mActiveCellRow * size);
            float right = (mActiveCellCol * size + size);
            float bottom = (mActiveCellRow * size + size);
            canvas.drawRect(left, top, right, bottom, mActiveCellPaint);
            left += size/8;
            top += size/8;
            right -= size/8;
            bottom -= size/8;
            final Cell cell = pond[mActiveCellCol][mActiveCellRow];
            if (cell.generation > 2 && cell.energy > 0) {
                mCellPaint.setColor(getColor(cell));
                canvas.drawRect(left, top, right, bottom, mCellPaint);
            } else {
                canvas.drawRect(left,  top, right, bottom, mBackgroundPaint);
            }
        }

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isInEditMode())
            return;

        Cat.d("size changed");

        mDrawMatrix.postTranslate(-NanoPond.POND_SIZE_X / 2, -NanoPond.POND_SIZE_Y / 2);

        // scale to display metrics
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        final float scale = 4 * metrics.density;
        mDrawMatrix.postScale(scale, scale);
        Cat.d("Size changed, new scale: " + scale);

        // move left-top position to middle of screen
        mDrawMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
    }

    static int[] artificial = { Color.WHITE, Color.GREEN, Color.CYAN,
            Color.YELLOW, Color.RED, Color.MAGENTA };

    static int cap(int i) {
        if (i > 255)
            return 255;
        else if (i < 0)
            return 0;
        else
            return i;
    }

    static int getColor(Cell cell) {
        if (cell.lineage < 0)
            return artificial[(int) Math.abs(cell.lineage) % artificial.length];

        int alpha = 0xff;
        int red = (int) cell.lineage % 256;
        int green = (int) cell.lineage % (256 * 256) / 256;
        int blue = (int) cell.lineage % (256 * 256 * 256) / 256 / 256;
        return (alpha << 24) | (cap(red) << 16) | (cap(green) << 8) | cap(blue);
    }
}













