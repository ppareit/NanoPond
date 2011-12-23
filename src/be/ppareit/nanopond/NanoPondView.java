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
import be.ppareit.android.GameLoopView;
import be.ppareit.nanopond.NanoPond.Cell;


/**
 *
 */
public class NanoPondView extends GameLoopView {

    static final String TAG = "NanoPondView";

    public enum State {
        RUNNING,
        PAUSED,
    }

    private State mState = State.PAUSED;

    private NanoPond mNanoPond = null;

    private Paint mCanvasPaint = null;
    private Paint mBackgroundPaint = null;

    private final int mXOffset = 0;
    private final int mYOffset = 0;

    public NanoPondView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;

        mNanoPond = ((NanoPondActivity) context).getNanoPond();

        mCanvasPaint = new Paint();
        mCanvasPaint.setColor(Color.GRAY);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.BLACK);


        setTargetFps(0);
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

    @Override
    protected void onUpdate() {
        // the nanopond object runs in its own thread, so no game logic updating needed
    }

    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;

    private final Paint mCellPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) return;

        final Cell[][] pond = mNanoPond.pond;

        final int cols = NanoPond.POND_SIZE_X;
        final int rows = NanoPond.POND_SIZE_Y;
        final int size = 1;

        canvas.drawRect(0, 0, getWidth(), getHeight(), mCanvasPaint);

        canvas.save();
        canvas.translate(mXOffset, mYOffset);
        canvas.scale(mScaleX, mScaleY);

        canvas.drawRect(0, 0, cols*size, rows*size, mBackgroundPaint);

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                final Cell cell = pond[c][r];
                if (cell.generation > 2 && cell.energy > 0) {
                    int left = (c*size);
                    int top = (r*size);
                    int right = (c*size + size);
                    int bottom = (r*size + size);
                    mCellPaint.setColor(getColor(pond[c][r]));
                    canvas.drawRect(left, top, right, bottom, mCellPaint);
                }
            }
        }

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isInEditMode()) return;
        mScaleX = (float)w/NanoPond.POND_SIZE_X;
        mScaleY = (float)h/NanoPond.POND_SIZE_Y;
    }



    static int[] artificial = { Color.WHITE, Color.GREEN, Color.CYAN, Color.YELLOW,
            Color.RED, Color.MAGENTA };

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





























