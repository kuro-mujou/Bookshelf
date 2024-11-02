package com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar

import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.geometry.Rect
import com.capstone.bookshelf.R

class CustomTextActionMode  (
    val onActionModeDestroy: (() -> Unit)? = null,
    var rect: Rect = Rect.Zero,
    var onTestRequested: (() -> Unit)? = null
) {
    fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu) { "onCreateActionMode requires a non-null menu" }
        requireNotNull(mode) { "onCreateActionMode requires a non-null mode" }

        onTestRequested?.let { addMenuItem(menu, MenuItemOption.CleanText) }
        return true
    }

    // this method is called to populate new menu items when the actionMode was invalidated
    fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        if (mode == null || menu == null) return false
        updateMenuItems(menu)
        // should return true so that new menu items are populated
        return true
    }

    fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            MenuItemOption.CleanText.id -> onTestRequested?.invoke()
            else -> return false
        }
        mode?.finish()
        return true
    }

    fun onDestroyActionMode(mode: ActionMode?) {
        onActionModeDestroy?.invoke()
    }

    @VisibleForTesting
    internal fun updateMenuItems(menu: Menu) {
        addOrRemoveMenuItem(menu, MenuItemOption.CleanText, onTestRequested)
    }

    private fun addMenuItem(menu: Menu, item: MenuItemOption) {
        menu
            .add(0, item.id, item.order, item.titleResource)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }
    @Suppress("SameParameterValue")
    private fun addOrRemoveMenuItem(menu: Menu,  item: MenuItemOption, callback: (() -> Unit)?) {
        when {
            callback != null && menu.findItem(item.id) == null -> addMenuItem(menu, item)
            callback == null && menu.findItem(item.id) != null -> menu.removeItem(item.id)
        }
    }
}

enum class MenuItemOption(val id: Int) {
    CleanText(0);

    val titleResource: Int
        get() =
            when (this) {
                CleanText -> R.string.CleanText
            }

    val order = id
}