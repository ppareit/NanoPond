package be.ppareit.nanopond;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.ListView;

public class NanoPondActivity extends Activity {

    private static final String TAG = NanoPondActivity.class.getSimpleName();

    private NanoPond nanopond = null;

    private View mainView;
    private NanoPondView gridView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nanopond = new NanoPond();
        nanopond.run();

        setContentView(R.layout.main);
        mainView = findViewById(R.id.main);
        gridView = (NanoPondView)findViewById(R.id.nanopond_view);


        ListView propertyList = (ListView)findViewById(R.id.property_list);
        ReportListAdapter rla = new ReportListAdapter(this, nanopond);
        propertyList.setAdapter(rla);

        final View raportView = findViewById(R.id.report_view);
        makeViewFloatable(raportView);


    }

    NanoPond getNanoPond() {
        return nanopond;
    }

    /**
     * This makes the given existing childView floatable on top of the mainView.
     * @param childView Child view to make floatable
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
                v.startDrag(null, new View.DragShadowBuilder(v), v, 0);
                return true;
            }
        });
        // the main view repositions the child views
        mainView.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    Log.v(TAG, "onDrag : ACTION_DRAG_STARTED");
                    // need to return true to keep getting drag/drop related messages
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    Log.v(TAG, "onDrag : ACTION_DRAG_ENDED");
                    // the child view is put in the localstate
                    View cv = (View)event.getLocalState();
                    // make visible again at the new position
                    cv.setVisibility(View.VISIBLE);
                    return true;
                }
                case DragEvent.ACTION_DROP: {
                    Log.v(TAG, "onDrag : ACTION_DROP");
                    // the child view is put in the localstate
                    View cv = (View)event.getLocalState();
                    // calling setLeft/setTop only works if layout is not yet set
                    // at 'runtime', we need to use the setTranslationX/Y functions
                    float dx = event.getX() - cv.getLeft() - cv.getWidth()/2;
                    float dy = event.getY() - cv.getTop() - cv.getHeight()/2;
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
        gridView.setMode(NanoPondView.State.PAUSED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gridView.setMode(NanoPondView.State.RUNNING);
    }
}