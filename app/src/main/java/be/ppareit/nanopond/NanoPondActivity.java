package be.ppareit.nanopond;

import static be.ppareit.StringLib.isHexString;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import be.ppareit.nanopond.NanoPond.Cell;
import be.ppareit.nanopond.R.id;

public class NanoPondActivity extends Activity {

    private static final String TAG = NanoPondActivity.class.getSimpleName();

    private static final int DIALOG_EDITCELL = 0x010;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_editcell) {
            showDialog(DIALOG_EDITCELL);
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_EDITCELL:
            createEditCellDialog();
            break;
        }
        return super.onCreateDialog(id);
    }

    NanoPond getNanoPond() {
        return mNanopond;
    }

    void createEditCellDialog() {
        Log.d(TAG, "Creating the editcell dialog");
        if (mGridView.isCellActive() == false) {
            Toast.makeText(this, R.string.no_cell_active_msg, Toast.LENGTH_LONG).show();
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.editcell);
        dialog.setTitle(R.string.menu_edit_title);
        final TextView hexaText = (TextView) dialog.findViewById(id.hexa_edit);
        final Button okButton = (Button) dialog.findViewById(id.ok);
        final int activeX = mGridView.getActiveCellCol();
        final int activeY = mGridView.getActiveCellRow();
        final Cell activeCell = mNanopond.pond[activeX][activeY];
        hexaText.setText(activeCell.getHexa());
        hexaText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > NanoPond.POND_DEPTH) {
                    okButton.setEnabled(false);
                } else if (isHexString(s.toString()) == false) {
                    okButton.setEnabled(false);
                } else {
                    okButton.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activeCell.setGenome(hexaText.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.show();
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
                        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
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
