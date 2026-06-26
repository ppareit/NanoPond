package be.ppareit.nanopond.gui

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.ppareit.android.openRawTextFile
import be.ppareit.nanopond.DetailListAdapter
import be.ppareit.nanopond.R
import be.ppareit.nanopond.ReportListAdapter
import be.ppareit.nanopond.core.NanoPond
import be.ppareit.nanopond.utils.isHex
import net.vrallev.android.cat.Cat
import java.io.IOException
import kotlin.math.roundToInt

class NanoPondActivity : ComponentActivity() {

    val nanoPond: NanoPond = NanoPond()

    private var nanoPondView: NanoPondView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        nanoPond.run()

        setContent {
            NanoPondApp()
        }
    }

    @Composable
    private fun NanoPondApp() {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF6DD6C2),
                secondary = Color(0xFFFFD166),
                surface = Color(0xFF15201F),
                background = Color.Black
            )
        ) {
            var showReport by rememberSaveable { mutableStateOf(true) }
            var showDetail by rememberSaveable { mutableStateOf(true) }
            var pondView by remember { mutableStateOf<NanoPondView?>(null) }

            Scaffold(
                topBar = {
                    AppBar(
                        showReport = showReport,
                        showDetail = showDetail,
                        onRun = {
                            pondView?.setMode(NanoPondView.State.RUNNING)
                            nanoPond.run()
                        },
                        onPause = {
                            pondView?.setMode(NanoPondView.State.PAUSED)
                            nanoPond.pauze()
                        },
                        onEdit = { createEditCellDialog()?.show() },
                        onToggleReport = { showReport = !showReport },
                        onToggleDetail = { showDetail = !showDetail },
                        onHelp = ::showHelpDialog,
                        onFeedback = ::sendFeedback,
                        onAbout = ::showAboutDialog
                    )
                },
                containerColor = Color.Black
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { context ->
                            val view = NanoPondView(context, nanoPond)
                            nanoPondView = view
                            pondView = view
                            view.setMode(NanoPondView.State.RUNNING)
                            view
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (showReport) {
                        FloatingPanel(
                            title = stringResource(R.string.report),
                            initialX = 12f,
                            initialY = 84f + paddingValues.calculateTopPadding().value,
                            height = 226
                        ) {
                            AndroidView(
                                factory = { context ->
                                    ListView(context).apply {
                                        adapter = ReportListAdapter(this@NanoPondActivity, nanoPond)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    val currentPondView = pondView
                    if (showDetail && currentPondView != null) {
                        FloatingPanel(
                            title = stringResource(R.string.detail),
                            initialX = 12f,
                            initialY = 326f + paddingValues.calculateTopPadding().value,
                            height = 282
                        ) {
                            AndroidView(
                                factory = { context ->
                                    ListView(context).apply {
                                        adapter = DetailListAdapter(
                                            this@NanoPondActivity,
                                            currentPondView,
                                            nanoPond
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppBar(
        showReport: Boolean,
        showDetail: Boolean,
        onRun: () -> Unit,
        onPause: () -> Unit,
        onEdit: () -> Unit,
        onToggleReport: () -> Unit,
        onToggleDetail: () -> Unit,
        onHelp: () -> Unit,
        onFeedback: () -> Unit,
        onAbout: () -> Unit
    ) {
        var menuExpanded by remember { mutableStateOf(false) }

        CenterAlignedTopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xEE101817),
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            actions = {
                IconButton(onClick = onRun) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_media_play),
                        contentDescription = stringResource(R.string.run_title)
                    )
                }
                IconButton(onClick = onPause) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_media_pause),
                        contentDescription = stringResource(R.string.pause_title)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_edit),
                        contentDescription = stringResource(R.string.edit_title)
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Text("...", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(toggleLabel(stringResource(R.string.report), showReport)) },
                            onClick = {
                                menuExpanded = false
                                onToggleReport()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(toggleLabel(stringResource(R.string.detail), showDetail)) },
                            onClick = {
                                menuExpanded = false
                                onToggleDetail()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.help_title)) },
                            onClick = {
                                menuExpanded = false
                                onHelp()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.feedback_title)) },
                            onClick = {
                                menuExpanded = false
                                onFeedback()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about_title)) },
                            onClick = {
                                menuExpanded = false
                                onAbout()
                            }
                        )
                    }
                }
            }
        )
    }

    @Composable
    private fun FloatingPanel(
        title: String,
        initialX: Float,
        initialY: Float,
        height: Int,
        content: @Composable () -> Unit
    ) {
        var offsetX by rememberSaveable { mutableFloatStateOf(initialX) }
        var offsetY by rememberSaveable { mutableFloatStateOf(initialY) }

        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .widthIn(min = 248.dp, max = 300.dp)
                .height(height.dp)
                .border(1.dp, Color(0xAAFFFFFF), MaterialTheme.shapes.small)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceAtLeast(0f)
                        offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
                    }
                },
            shape = MaterialTheme.shapes.small,
            color = Color(0xDD1F2933),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color(0xFF6DD6C2))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .width(280.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF0B1110),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                Box(
                    modifier = Modifier
                        .padding(top = 34.dp)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }

    private fun toggleLabel(label: String, checked: Boolean): String {
        return if (checked) "Hide $label" else "Show $label"
    }

    private fun showHelpDialog() {
        try {
            val message = resources.openRawTextFile(R.raw.help)
            val webView = WebView(this)
            webView.loadDataWithBaseURL(
                null,
                message.toString(),
                "text/html",
                "utf-8",
                null
            )
            AlertDialog.Builder(this)
                .setTitle(R.string.help_title)
                .setView(webView)
                .setPositiveButton(getText(android.R.string.ok), null)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun sendFeedback() {
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
            Toast.makeText(this, R.string.unable_to_start_mail_client, Toast.LENGTH_LONG).show()
        }
    }

    private fun showAboutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.about_dlg_title)
            .setMessage(R.string.about_dlg_message)
            .setPositiveButton(getText(android.R.string.ok), null)
            .create()
        dialog.show()
        Linkify.addLinks(
            dialog.findViewById<View>(android.R.id.message) as TextView,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
        )
    }

    private fun createEditCellDialog(): Dialog? {
        Cat.d("Creating the edit cell dialog")
        val view = nanoPondView
        if (view == null || !view.isCellActive) {
            Toast.makeText(this, R.string.no_cell_active_msg, Toast.LENGTH_LONG).show()
            return null
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.editcell)
        dialog.setTitle(R.string.edit_title)
        val hexaText = dialog.findViewById<TextView>(R.id.hexa_edit)
        val okButton = dialog.findViewById<Button>(R.id.ok)
        val activeX = view.activeCellCol
        val activeY = view.activeCellRow
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
        return dialog
    }

    override fun onPause() {
        super.onPause()
        nanoPondView?.setMode(NanoPondView.State.PAUSED)
    }

    override fun onResume() {
        super.onResume()
        nanoPondView?.setMode(NanoPondView.State.RUNNING)
    }
}
