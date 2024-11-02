package com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.ActionMode
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

class CustomTextToolbar(
    private val view: View,
    private val scrollingPager: MutableState<Boolean>,
    private val enableScaffoldBar : MutableState<Boolean>
) : TextToolbar {
    private var actionMode: ActionMode? = null
    private val textActionModeCallback: CustomTextActionMode =
        CustomTextActionMode(onActionModeDestroy = { actionMode = null })
    var context: Context = view.context
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
//            Log.d("CustomTextToolbar", clipboard.primaryClip?.getItemAt(0)?.text.toString())
            clipboard.clearPrimaryClip()
            scrollingPager.value = true
            enableScaffoldBar.value = true
        }
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode =
                TextToolbarHelperMethods.startActionMode(
                    view,
                    CustomFloatingActionMode(textActionModeCallback),
                    ActionMode.TYPE_FLOATING
                )
            scrollingPager.value = false
            enableScaffoldBar.value = false
        } else {
            actionMode?.invalidate()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        scrollingPager.value = true
        enableScaffoldBar.value = true
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
//class CustomTextToolbar(
//    private val view: View,
//    private val scrollingPager: MutableState<Boolean>,
//    private val enableScaffoldBar : MutableState<Boolean>,
//    private val clipboard: ClipboardManager
//) : TextToolbar {
//    private var actionMode: ActionMode? = null
//    private val textActionModeCallback: CustomTextActionMode =
//        CustomTextActionMode(
//            onActionModeDestroy = { actionMode = null },
//            view = view,
//        )
//
//    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
//        private set
//    private var popupMenu: CustomPopupMenu? = null
//
//    override fun showMenu(
//        rect: androidx.compose.ui.geometry.Rect,
//        onCopyRequested: (() -> Unit)?,
//        onPasteRequested: (() -> Unit)?,
//        onCutRequested: (() -> Unit)?,
//        onSelectAllRequested: (() -> Unit)?
//    ) {
//        // AnchorView is the 'view' passed in the constructor
//        popupMenu = CustomPopupMenu(
//            context = view.context, // Context of the anchor view
//            anchorView = view, // This is the view to anchor the PopupWindow to
//            onCleanTextRequested = {
//                // This is the logic that runs when 'Clean Text' is clicked
//                clipboard.setText(AnnotatedString("")) // Clear clipboard text
//                onCopyRequested?.invoke() // Invoke the 'copy' logic
//            },
//            actionMode= actionMode
//        )
//
//        popupMenu?.showMenu() // Show the custom popup menu
//        scrollingPager.value = false
//        enableScaffoldBar.value = false
//    }
////    @RequiresApi(Build.VERSION_CODES.P)
////    override fun showMenu(
////        rect: Rect,
////        onCopyRequested: (() -> Unit)?,
////        onPasteRequested: (() -> Unit)?,
////        onCutRequested: (() -> Unit)?,
////        onSelectAllRequested: (() -> Unit)?
////    ) {
////        textActionModeCallback.rect = rect
////        textActionModeCallback.onCleanTextRequested = {
////            Log.d("CustomTextToolbar", "Test button clicked")
////            onCopyRequested?.invoke()
////            Log.d("CustomTextToolbar", clipboard.getText().toString())
////            clipboard.setText(AnnotatedString(""))
////        }
////        if (actionMode == null) {
////            status = TextToolbarStatus.Shown
////            actionMode =
////                TextToolbarHelperMethods.startActionMode(
////                    view,
////                    CustomFloatingActionMode(textActionModeCallback),
////                    ActionMode.TYPE_FLOATING
////                )
////            scrollingPager.value = false
////            enableScaffoldBar.value = false
////        } else {
////            actionMode?.invalidate()
////        }
////    }
//
//    override fun hide() {
//        status = TextToolbarStatus.Hidden
//        actionMode?.finish()
//        scrollingPager.value = true
//        enableScaffoldBar.value = true
//        actionMode = null
//    }
//}
//
//object TextToolbarHelperMethods {
//    fun startActionMode(
//        view: View,
//        actionModeCallback: ActionMode.Callback,
//        type: Int
//    ): ActionMode? {
//        return view.startActionMode(actionModeCallback, type)
//    }
//}