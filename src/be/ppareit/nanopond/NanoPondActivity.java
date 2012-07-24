package be.ppareit.nanopond;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ListView;

public class NanoPondActivity extends Activity {

    private static final String TAG = NanoPondActivity.class.getSimpleName();

    private NanoPond mNanopond = null;

    private View mMainView;
    private NanoPondView mGridView;
    private View mRaportView;
    private View mDetailView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNanopond = new NanoPond();
        mNanopond.run();

        setContentView(R.layout.main);
        mMainView = findViewById(R.id.main);
        mGridView = (NanoPondView) findViewById(R.id.nanopond_view);

        ListView propertyList = (ListView) findViewById(R.id.report_property_list);
        ReportListAdapter rla = new ReportListAdapter(this, mNanopond);
        propertyList.setAdapter(rla);

        mRaportView = findViewById(R.id.report_view);
        makeViewFloatable(mRaportView);

        ListView propertyList2 = (ListView) findViewById(R.id.detail_property_list);
        DetailListAdapter dla = new DetailListAdapter(this, mGridView, mNanopond);
        propertyList2.setAdapter(dla);

        mDetailView = findViewById(R.id.detail_view);
        makeViewFloatable(mDetailView);

    }

    NanoPond getNanoPond() {
        return mNanopond;
    }

    /**
     * This makes the given existing childView floatable on top of the mainView.
     *
     * @param childView
     *            Child view to make floatable
     */
    private void makeViewFloatable(View childView) {
        // the child view begins window dragging on a long click
        childView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "onLongClick");
                // make invisible
                v.setVisibility(View.INVISIBLE);
                // the dragshadowbuilder will display an outline
                v.startDrag(null, new View.DragShadowBuilder(v) {
                    @Override
                    public void onProvideShadowMetrics(Point shadowSize,
                            Point shadowTouchPoint) {
                        super.onProvideShadowMetrics(shadowSize,
                                shadowTouchPoint);
                        shadowTouchPoint.y = 10;
                    }
                }, v, 0);
                return true;
            }
        });
        // the main view repositions the child views
        mMainView.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    Log.v(TAG, "onDrag : ACTION_DRAG_STARTED");
                    // need to return true to keep getting drag/drop related
                    // messages
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    Log.v(TAG, "onDrag : ACTION_DRAG_ENDED");
                    // the child view is put in the localstate
                    View cv = (View) event.getLocalState();
                    // make visible again at the new position
                    cv.setVisibility(View.VISIBLE);
                    return true;
                }
                case DragEvent.ACTION_DROP: {
                    Log.v(TAG, "onDrag : ACTION_DROP");
                    // the child view is put in the localstate
                    View cv = (View) event.getLocalState();
                    // calling setLeft/setTop only works if layout is not yet
                    // set at 'runtime', we need to use the setTranslationX/Y
                    // functions
                    float dx = event.getX() - cv.getLeft() - cv.getWidth() / 2;
                    float dy = event.getY() - cv.getTop() - 10;
                    cv.setTranslationX(dx);
                    cv.setTranslationY(dy);
                    return true;
                }
                default:
                    return false;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGridView.setMode(NanoPondView.State.PAUSED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGridView.setMode(NanoPondView.State.RUNNING);
    }
}











