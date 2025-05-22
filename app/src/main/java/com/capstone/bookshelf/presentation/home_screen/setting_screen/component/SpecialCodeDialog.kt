package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.R

@Composable
fun SpecialCodeDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    val couponCode = "GARDEN OF DICK"
    var inputText by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        val focusManager = LocalFocusManager.current
        val context = LocalContext.current
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusManager.clearFocus()
                    }
                ),
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ){
                Text(
                    modifier = Modifier
                        .padding(bottom = 4.dp),
                    text = "Coupon Code",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                HorizontalDivider(thickness = 2.dp)
                OutlinedTextField(
                    shape = RoundedCornerShape(8.dp),
                    value = inputText,
                    onValueChange = {
                        inputText = it
                    },
                    label = {
                        Text(text = "Enter your coupon code")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText == couponCode) {
                                    onSuccess()
                                    Toast.makeText(context, "Coupon code applied", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Invalid coupon code", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_send),
                                contentDescription = "Check"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}