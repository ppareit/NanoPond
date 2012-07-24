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
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import be.ppareit.android.GameLoopView;
import be.ppareit.nanopond.NanoPond.Cell;

/**
 *
 */
public class NanoPondView extends GameLoopView {

    static final String TAG = "NanoPondView";

    public enum State {
        RUNNING, PAUSED,
    }

    private State mState = State.PAUSED;

    private NanoPond mNanoPond = null;

    private Paint mCanvasPaint = null;
    private Paint mBackgroundPaint = null;

    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private float mLastTouchX;
    private float mLastTouchY;

    private int mXOffset = 0;
    private int mYOffset = 0;

    // Scale calculated to fill screen initially
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;

    private final Paint mCellPaint = new Paint();

    private final Paint mActiveCellPaint = new Paint();

    // Useful paint to display dubugging info on screen
    private final Paint mDebugPaint = new Paint();

    // Scale by user
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    // keeps track when the user moves the display
    boolean mPanInProgress = false;

    // keep track of the active cell
    private int mActiveCellCol = -1;
    private int mActiveCellRow = -1;

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

        mScaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        final float oldScaleFactor = mScaleFactor;
                        mScaleFactor *= detector.getScaleFactor();
                        // limit the scaling
                        mScaleFactor = Math.max(0.5f, mScaleFactor);
                        mScaleFactor = Math.min(20.0f, mScaleFactor);
                        // reposition
                        // TODO: further improve centerposition
                        final int cols = NanoPond.POND_SIZE_X;
                        final int rows = NanoPond.POND_SIZE_Y;
                        final float focusX = detector.getFocusX();
                        final float focusY = detector.getFocusY();
                        final float width = getWidth();
                        final float height = getHeight();
                        mXOffset += mScaleX * cols
                                * (oldScaleFactor - mScaleFactor)
                                / (width / focusX);
                        mYOffset += mScaleY * rows
                                * (oldScaleFactor - mScaleFactor)
                                / (height / focusY);
                        return true;
                    }
                });
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
        // the nanopond object runs in its own thread
        // thus that is updated continuously

        // try to remove the margins
        if (mPanInProgress == false) {
            if (mXOffset > 0) {
                mXOffset -= mXOffset / 2;
            }
            final int cols = NanoPond.POND_SIZE_X;
            final float rightMargin = getWidth() - mXOffset - cols
                    * mScaleFactor * mScaleX;
            if (rightMargin > 0) {
                mXOffset += rightMargin / 2;
            }
            if (mYOffset > 0) {
                mYOffset -= mYOffset / 2;
            }
            final int rows = NanoPond.POND_SIZE_Y;
            final float bottomMargin = getHeight() - mYOffset - rows
                    * mScaleFactor * mScaleY;
            if (bottomMargin > 0) {
                mYOffset += bottomMargin / 2;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            // started moving the display
            mPanInProgress = true;
            // store touch data
            mLastTouchX = event.getX();
            mLastTouchY = event.getY();
            mActivePointerId = event.getPointerId(0);
            // set the active cell
            mActiveCellCol = (int) ((mLastTouchX - mXOffset)/(mScaleX*mScaleFactor));
            mActiveCellRow = (int) ((mLastTouchY - mYOffset)/(mScaleY*mScaleFactor));
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = event.findPointerIndex(mActivePointerId);
            final float x = event.getX(pointerIndex);
            final float y = event.getY(pointerIndex);

            if (!mScaleGestureDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mXOffset += dx;
                mYOffset += dy;
            }

            mLastTouchX = x;
            mLastTouchY = y;

            break;
        }
        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            mPanInProgress = false;
            break;
        }
        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            mPanInProgress = false;
            break;
        }
        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = event.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                final int newPointerIndex = (pointerIndex == 0 ? 1 : 0);
                mLastTouchX = event.getX(newPointerIndex);
                mLastTouchY = event.getY(newPointerIndex);
                mActivePointerId = event.getPointerId(newPointerIndex);
            }
            break;
        }

        }

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
        canvas.translate(mXOffset, mYOffset);
        canvas.scale(mScaleX, mScaleY);
        canvas.scale(mScaleFactor, mScaleFactor);

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
        mScaleX = (float) w / NanoPond.POND_SIZE_X;
        mScaleY = (float) h / NanoPond.POND_SIZE_Y;
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













