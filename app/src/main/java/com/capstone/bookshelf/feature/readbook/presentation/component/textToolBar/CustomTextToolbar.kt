package com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.ActionMode
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class CustomTextToolbar(
    private val view: View,
    val regexSelected: (String) -> Unit,
    val updateState: (Boolean,Boolean) -> Unit
) : TextToolbar {
    private var actionMode: ActionMode? = null
    private val textActionModeCallback: CustomTextActionMode =
        CustomTextActionMode(onActionModeDestroy = { actionMode = null })
    private var context: Context = view.context
    private var clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    @RequiresApi(Build.VERSION_CODES.P)
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        textActionModeCallback.rect = rect
        textActionModeCallback.onTestRequested = {
            onCopyRequested?.invoke()
            regexSelected(clipboard.primaryClip?.getItemAt(0)?.text.toString())
            val clipData = ClipData.newPlainText(null,"")
            clipboard.setPrimaryClip(clipData)
            updateState(true,true)
        }
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode =
                TextToolbarHelperMethods.startActionMode(
                    view,
                    CustomFloatingActionMode(textActionModeCallback),
                    ActionMode.TYPE_FLOATING
                )
            updateState(false,false)
        } else {
            actionMode?.invalidate()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        updateState(true,true)
        actionMode = null
    }
}

object TextToolbarHelperMethods {
    fun startActionMode(
        view: View,
        actionModeCallback: ActionMode.Callback,
        type: Int
    ): ActionMode? {
        return view.startActionMode(actionModeCallback, type)
    }
    fun invalidateContentRect(actionMode: ActionMode) {
        actionMode.invalidateContentRect()
    }
}