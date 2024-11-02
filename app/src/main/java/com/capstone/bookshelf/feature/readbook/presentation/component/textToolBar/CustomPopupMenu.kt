package com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar

import android.content.Context
import android.view.ActionMode
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import com.capstone.bookshelf.R


class CustomPopupMenu(
    private val context: Context,
    private val anchorView: View, // View to anchor the PopupWindow
    private val onCleanTextRequested: (() -> Unit)?, // Callback when clean text is selected
    private val actionMode : ActionMode?
) {
    private var popupWindow: PopupWindow? = null
    private var popup: PopupMenu = PopupMenu(context, anchorView)

    fun showMenu() {
        // Inflate the custom layout for the popup menu
//        actionMode?.menuInflater?.inflate(R.menu.menu_item_custom, popup.menu)
//        popup.menuInflater.inflate(R.menu.menu_item_custom, popup.menu)
//        popup.show()
//        actionMode?.menu?.let {
//            popup.show()
//        }
//        val inflater = LayoutInflater.from(context)
//        val layout = inflater.inflate(R.layout.menu_item_custom, anchorView.rootView as ViewGroup, false)
//
//        // Find and configure the icon and text in the inflated layout
//        val iconView = layout.findViewById<ImageView>(R.id.menu_item_icon)
//        val textView = layout.findViewById<TextView>(R.id.menu_item_text)
//
//        // Set the text and icon (you can modify it as needed)
//        iconView.setImageResource(R.drawable.ic_clean_text) // Your icon resource
//        textView.setText(R.string.CleanText) // Your string resource
//
//        // Create the PopupWindow with the custom layout
//        popupWindow = PopupWindow(
//            layout,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true // Make it focusable
//        )
//
//        // Handle clicking on the popup item
//        layout.setOnClickListener {
//            onCleanTextRequested?.invoke() // Call the callback when the item is clicked
//            popupWindow?.dismiss() // Dismiss the popup
//        }
//
//        // Show the popup window anchored to the view
//        popupWindow?.showAsDropDown(anchorView)
    }

    fun dismissMenu() {
        popup.dismiss()
    }
}