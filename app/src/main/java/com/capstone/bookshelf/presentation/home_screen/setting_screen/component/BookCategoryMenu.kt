package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Category
import com.capstone.bookshelf.presentation.component.BookChip
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingAction
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookCategoryMenu(
    settingState: SettingState,
    onAction: (SettingAction) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    var categoryName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var selectedColorSet by remember { mutableIntStateOf(0) }
    var selectedColor by remember(isDarkTheme) {
        mutableStateOf(if (isDarkTheme) Color(0xFFE57373) else Color(0xFFEF9A9A))
    }
    val isImeVisible = WindowInsets.isImeVisible
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.safeContent
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    focusManager.clearFocus()
                }
            )
    ) {
        Text(
            text = "BOOK CATEGORY",
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
            )
        )
        AnimatedVisibility(
            visible = isImeVisible
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
                BookChip(
                    selected = true,
                    color = selectedColor,
                    onClick = {}
                ) {
                    Text(text = "Category")
                }
                BookChip(
                    selected = false,
                    color = selectedColor,
                    onClick = {}
                ) {
                    Text(text = "Category")
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
                placeholder = { Text("Add new category") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onAction(
                                    SettingAction.AddCategory(
                                        Category(
                                            name = categoryName.trim(),
                                            color = selectedColor.toArgb()
                                        )
                                    )
                                )
                                categoryName = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_send),
                            contentDescription = "Add new category",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(25.dp)
            )
            IconButton(
                onClick = {
                    onAction(SettingAction.DeleteCategory)
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                    contentDescription = "delete",
                    tint = if (isSystemInDarkTheme())
                        Color(250, 160, 160)
                    else
                        Color(194, 59, 34)
                )
            }
        }
        AnimatedVisibility(
            visible = isImeVisible
        ) {
            ColorRails(
                selectedColorSet = selectedColorSet,
                onClick = { index, color ->
                    selectedColorSet = index
                    selectedColor = color
                }
            )
        }
        Text(
            text = "Tap to select category, selected categories can be delete",
            modifier = Modifier
                .fillMaxWidth(),
            style = TextStyle(
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
            )
        )
        FlowRow(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            settingState.bookCategories.forEach { categoryChip ->
                BookChip(
                    selected = categoryChip.isSelected,
                    color = Color(categoryChip.color),
                    onClick = {
                        onAction(SettingAction.ChangeChipState(categoryChip))
                    }
                ) {
                    Text(text = categoryChip.name)
                }
            }
        }
    }
}