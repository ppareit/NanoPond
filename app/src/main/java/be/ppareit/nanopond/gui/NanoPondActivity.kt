package be.ppareit.nanopond.gui

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.view.DragEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import be.ppareit.android.openRawTextFile
import be.ppareit.android.toggleChecked
import be.ppareit.android.toggleVisibility
import be.ppareit.nanopond.DetailListAdapter
import be.ppareit.nanopond.NanoPond
import be.ppareit.nanopond.R
import be.ppareit.nanopond.R.id
import be.ppareit.nanopond.ReportListAdapter
import be.ppareit.nanopond.utils.isHex
import net.vrallev.android.cat.Cat
import java.io.IOException

private const val DIALOG_EDIT_CELL = 0x010

class NanoPondActivity : AppCompatActivity() {

    val nanoPond: NanoPond = NanoPond()

    private val mainView: View by lazy { findViewById(R.id.mainView) }
    private val nanoPondView: NanoPondView by lazy { findViewById(R.id.nanoPondView) }
    private val reportView: View by lazy { findViewById(R.id.reportView) }
    private val detailView: View by lazy { findViewById(R.id.detailView) }

    private val reportPropertyList: ListView by lazy { findViewById(R.id.reportPropertyList) }
    private val detailPropertyList: ListView by lazy { findViewById(R.id.detailPropertyList) }

    public override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        nanoPond.run()

        setContentView(R.layout.main)

        val rla = ReportListAdapter(this, nanoPond)
        reportPropertyList.adapter = rla
        makeViewFloatable(reportView)

        val dla = DetailListAdapter(this, nanoPondView, nanoPond)
        detailPropertyList.adapter = dla
        makeViewFloatable(detailView)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            id.action_show_report -> {
                reportView.toggleVisibility()
                item.toggleChecked()
            }

            id.action_show_detail -> {
                detailView.toggleVisibility()
                item.toggleChecked()
            }

            id.action_help -> try {
                val res = resources
                val message = res.openRawTextFile(R.raw.help)
                val webView = WebView(this)
                webView.loadDataWithBaseURL(
                    null, message.toString(),
                    "text/html", "utf-8", null
                )
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle(R.string.help_title)
                    .setView(webView)
                    .setPositiveButton(getText(android.R.string.ok), null)
                    .create()
                alertDialog.show()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            id.action_feedback -> {
                val to = "pieter.pareit@gmail.com"
                val subject = "Nanopond feedback"
                val message = ("Device: " + Build.MODEL + "\nAndroid version: "
                        + Build.VERSION.RELEASE + "\nFeedback: \n")
                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                email.putExtra(Intent.EXTRA_SUBJECT, subject)
                email.putExtra(Intent.EXTRA_TEXT, message)
                email.type = "message/rfc822"
                try {
                    startActivity(email)
                } catch (exception: ActivityNotFoundException) {
                    Toast.makeText(this, R.string.unable_to_start_mail_client, Toast.LENGTH_LONG)
                        .show()
                }

            }

            id.action_about -> {
                val ad = AlertDialog.Builder(this)
                    .setTitle(R.string.about_dlg_title)
                    .setMessage(R.string.about_dlg_message)
                    .setPositiveButton(getText(android.R.string.ok), null)
                    .create()
                ad.show()
                Linkify.addLinks(
                    ad.findViewById<View>(android.R.id.message) as TextView,
                    Linkify.ALL
                )
            }

            id.action_run -> {
                nanoPondView.setMode(NanoPondView.State.RUNNING)
                nanoPond.run()
            }

            id.action_pause -> {
                nanoPondView.setMode(NanoPondView.State.PAUSED)
                nanoPond.pauze()
            }

            id.action_editcell -> showDialog(DIALOG_EDIT_CELL)
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            DIALOG_EDIT_CELL -> return createEditCellDialog()
        }
        return super.onCreateDialog(id)
    }

    internal fun createEditCellDialog(): Dialog? {
        Cat.d("Creating the edit cell dialog")
        if (!nanoPondView.isCellActive) {
            Toast.makeText(this, R.string.no_cell_active_msg, Toast.LENGTH_LONG).show()
            return null
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.editcell)
        dialog.setTitle(R.string.edit_title)
        val hexaText = dialog.findViewById<TextView>(id.hexa_edit)
        val okButton = dialog.findViewById<Button>(id.ok)
        val activeX = nanoPondView.activeCellCol
        val activeY = nanoPondView.activeCellRow
        val activeCell = nanoPond.pond[activeX][activeY]
        hexaText.text = activeCell.hexa
        hexaText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                okButton.isEnabled = s.length > NanoPond.POND_DEPTH && s.isHex()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })
        okButton.setOnClickListener {
            activeCell.setGenome(hexaText.text.toString())
            dialog.dismiss()
        }
        dialog.show()
        return dialog
    }

    /**
     * This makes the given existing childView floatable on top of the mainView.
     *
     * @param childView Child view to make floatable
     */
    private fun makeViewFloatable(childView: View) {
        // the child view begins window dragging on a long click
        childView.setOnLongClickListener { v ->
            Cat.v("onLongClick")
            // make invisible
            v.visibility = View.INVISIBLE
            // the dragshadowbuilder will display an outline
            v.startDrag(null, object : View.DragShadowBuilder(v) {
                override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
                    super.onProvideShadowMetrics(shadowSize, shadowTouchPoint)
                    shadowTouchPoint.y = 10
                }
            }, v, 0)
            true
        }
        // the main view repositions the child views
        mainView.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Cat.v("onDrag : ACTION_DRAG_STARTED")
                    // need to return true to keep getting drag/drop related
                    // messages
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    Cat.v("onDrag : ACTION_DRAG_ENDED")
                    // the child view is put in the localstate
                    val cv = event.localState as View
                    // make visible again at the new position
                    cv.visibility = View.VISIBLE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    Cat.v("onDrag : ACTION_DROP")
                    // the child view is put in the localstate
                    val cv = event.localState as View
                    // calling setLeft/setTop only works if layout is not yet
                    // set at 'runtime', we need to use the setTranslationX/Y
                    // functions
                    val dx = event.x - cv.left.toFloat() - (cv.width / 2).toFloat()
                    val dy = event.y - cv.top.toFloat() - 10f
                    cv.translationX = dx
                    cv.translationY = dy
                    true
                }

                else -> false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nanoPondView.setMode(NanoPondView.State.PAUSED)
    }

    override fun onResume() {
        super.onResume()
        nanoPondView.setMode(NanoPondView.State.RUNNING)
    }

}
