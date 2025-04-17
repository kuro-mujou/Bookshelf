package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.component.BookmarkShape
import com.capstone.bookshelf.util.darken
import com.capstone.bookshelf.util.isDark
import com.capstone.bookshelf.util.lighten
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun BookmarkCard(
    bookmarkContent : String,
    bookmarkIndex : Int,
    contentState : ContentState,
    colorPaletteState: ColorPalette,
    bookmarkStyle: BookmarkStyle,
    onCardClicked: (Int) -> Unit,
    onDeleted: ((Int) -> Unit)? = null
){
    val baseColor = if(colorPaletteState.containerColor.isDark()){
        colorPaletteState.containerColor.lighten(0.2f)
    }else{
        colorPaletteState.containerColor.darken(0.2f)
    }
    val tooltipState = rememberTooltipState(
        isPersistent = true
    )
    val isBackgroundReady = remember { mutableStateOf(false) }

    LaunchedEffect(bookmarkIndex) {
        delay(500)
        isBackgroundReady.value = true
    }
    val cardComposable : @Composable () -> Unit = {
        ElevatedCard(
            shape = BookmarkShape(),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colorPaletteState.textColor.copy(0.3f),
                    shape = BookmarkShape()
                )
                .clickable {
                    onCardClicked(bookmarkIndex)
                },
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorPaletteState.backgroundColor,
                contentColor = colorPaletteState.textColor,
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.matchParentSize()
                ) {
                    AnimatedVisibility(
                        visible = isBackgroundReady.value,
                        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                    ) {
                        when (bookmarkStyle) {
                            BookmarkStyle.WAVE_WITH_BIRDS -> {
                                CardBackgroundWaveWithBirds(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.CLOUD_WITH_BIRDS -> {
                                CardBackgroundCloudWithBirds(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.STARRY_NIGHT -> {
                                CardBackgroundStarryNight(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.GEOMETRIC_TRIANGLE -> {
                                CardBackgroundGeometricTriangle(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.POLYGONAL_HEXAGON -> {
                                CardBackgroundPolygonalHexagon(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.SCATTERED_HEXAGON -> {
                                CardBackgroundScatteredHexagons(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }

                            BookmarkStyle.CHERRY_BLOSSOM_RAIN -> {
                                CardBackgroundCherryBlossomRain(
                                    modifier = Modifier.fillMaxSize(),
                                    baseColor = baseColor
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .background(
                            color = colorPaletteState.backgroundColor,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            top = 4.dp,
                            end = 8.dp,
                            bottom = 4.dp
                        ),
                        text = bookmarkContent,
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                        )
                    )
                }
            }
        }
    }
    if (onDeleted != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                IconButton(
                    modifier = Modifier
                        .background(
                            color = colorPaletteState.textBackgroundColor,
                            shape = CircleShape
                        ),
                    onClick = {
                        onDeleted(bookmarkIndex)
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                        contentDescription = null
                    )
                }
            },
            state = tooltipState,
        ) {
            cardComposable()
        }
    } else {
        cardComposable()
    }
}
