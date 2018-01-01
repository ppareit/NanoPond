package be.ppareit.nanopond;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.vrallev.android.cat.Cat;

import java.io.IOException;

import be.ppareit.nanopond.R.id;

import static be.ppareit.StringLib.isHexString;
import static be.ppareit.android.Utils.openRawTextFile;

public class NanoPondActivity extends Activity {

    private static final int DIALOG_EDIT_CELL = 0x010;

    private NanoPond mNanopond = null;

    private View mMainView;
    private NanoPondView mGridView;
    private View mRapportView;
    private View mDetailView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNanopond = new NanoPond();
        mNanopond.run();

        setContentView(R.layout.main);
        mMainView = findViewById(R.id.main);
        mGridView = findViewById(R.id.nanopond_view);

        ListView propertyList = findViewById(R.id.report_property_list);
        ReportListAdapter rla = new ReportListAdapter(this, mNanopond);
        propertyList.setAdapter(rla);

        mRapportView = findViewById(R.id.report_view);
        makeViewFloatable(mRapportView);

        ListView propertyList2 = findViewById(R.id.detail_property_list);
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
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                try {
                    Resources res = getResources();
                    CharSequence message = openRawTextFile(res, R.raw.help);
                    WebView webView = new WebView(this);
                    webView.loadDataWithBaseURL(null, String.valueOf(message),
                            "text/html", "utf-8", null);
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.help_title)
                            .setView(webView)
                            .setPositiveButton(getText(android.R.string.ok), null)
                            .create();
                    alertDialog.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_feedback:
                String to = "pieter.pareit@gmail.com";
                String subject = "Nanopond feedback";
                String message = "Device: " + Build.MODEL + "\nAndroid version: "
                        + Build.VERSION.RELEASE + "\nFeedback: \n";
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                email.putExtra(Intent.EXTRA_TEXT, message);
                email.setType("message/rfc822");
                startActivity(email);

                break;
            case R.id.action_about:
                AlertDialog ad = new AlertDialog.Builder(this)
                        .setTitle(R.string.about_dlg_title)
                        .setMessage(R.string.about_dlg_message)
                        .setPositiveButton(getText(android.R.string.ok), null)
                        .create();
                ad.show();
                Linkify.addLinks((TextView) ad.findViewById(android.R.id.message),
                        Linkify.ALL);
                break;
            case R.id.action_run:
                mGridView.setMode(NanoPondView.State.RUNNING);
                mNanopond.run();
                break;
            case R.id.action_pause:
                mGridView.setMode(NanoPondView.State.PAUSED);
                mNanopond.pauze();
                break;
            case R.id.action_editcell:
                showDialog(DIALOG_EDIT_CELL);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_EDIT_CELL:
                createEditCellDialog();
                break;
        }
        return super.onCreateDialog(id);
    }

    NanoPond getNanoPond() {
        return mNanopond;
    }

    void createEditCellDialog() {
        Cat.d("Creating the edit cell dialog");
        if (!mGridView.isCellActive()) {
            Toast.makeText(this, R.string.no_cell_active_msg, Toast.LENGTH_LONG).show();
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.editcell);
        dialog.setTitle(R.string.edit_title);
        final TextView hexaText = dialog.findViewById(id.hexa_edit);
        final Button okButton = dialog.findViewById(id.ok);
        final int activeX = mGridView.getActiveCellCol();
        final int activeY = mGridView.getActiveCellRow();
        final Cell activeCell = mNanopond.pond[activeX][activeY];
        hexaText.setText(activeCell.getHexa());
        hexaText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > NanoPond.POND_DEPTH) {
                    okButton.setEnabled(false);
                } else if (!isHexString(s.toString())) {
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
        okButton.setOnClickListener(v -> {
            activeCell.setGenome(hexaText.getText().toString());
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * This makes the given existing childView floatable on top of the mainView.
     *
     * @param childView Child view to make floatable
     */
    private void makeViewFloatable(View childView) {
        // the child view begins window dragging on a long click
        childView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Cat.v("onLongClick");
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
        mMainView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    Cat.v("onDrag : ACTION_DRAG_STARTED");
                    // need to return true to keep getting drag/drop related
                    // messages
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    Cat.v("onDrag : ACTION_DRAG_ENDED");
                    // the child view is put in the localstate
                    View cv = (View) event.getLocalState();
                    // make visible again at the new position
                    cv.setVisibility(View.VISIBLE);
                    return true;
                }
                case DragEvent.ACTION_DROP: {
                    Cat.v("onDrag : ACTION_DROP");
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
