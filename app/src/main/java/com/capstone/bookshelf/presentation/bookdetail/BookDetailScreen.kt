package com.capstone.bookshelf.presentation.bookdetail

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookdetail.component.BookChip
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BookDetailScreenRoot(
    viewModel: BookDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var vibrantColor by remember { mutableStateOf(Color.Transparent) }
    val context = LocalContext.current
    var result by remember { mutableStateOf<Drawable?>(null) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    val style = HazeMaterials.ultraThin(Color(0xFF181C20))
    val hazeState = remember { HazeState() }
    LaunchedEffect(state.book) {
        val imageUrl =
            if (state.book?.coverImagePath == "error")
                R.mipmap.book_cover_not_available
            else
                state.book?.coverImagePath
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        result = (loader.execute(request) as? SuccessResult)?.drawable
        result?.toBitmap()?.let { bitmap ->
            Palette.from(bitmap).generate { palette ->
                val color = palette?.vibrantSwatch?.rgb
                if (color != null) {
                    vibrantColor = Color(color)
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(LocalDensity.current) { canvasHeight.toDp() + 8.dp })
                        .align(Alignment.BottomStart)
                        .blur(30.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                ) {
                    drawRect(
                        color = vibrantColor.copy(alpha = 0.5f),
                    )
                }
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { canvasHeight.toDp() })
                .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
            ) {
                AsyncImage(
                    model = result,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier
                                    .hazeSource(state = hazeState)
                            }
                            else
                                Modifier
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
                    .onGloballyPositioned {
                        canvasHeight = it.size.height.toFloat()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark),
                                contentDescription = null,
                                tint = if (state.book?.isFavorite == true)
                                    Color.Green
                                else
                                    Color.White,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(125.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 30.dp, bottomEnd = 8.dp))
                                .background(Color(0xFF156683).copy(0.5f))
                        ) {
                            AsyncImage(
                                model = result,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 30.dp, bottomEnd = 8.dp))
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 30.dp))
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier
                                            .background(Color.Transparent)
                                            .hazeEffect(
                                                state = hazeState,
                                                style = style
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = state.book?.title ?: "",
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                    fontWeight = FontWeight.Medium
                                ),
                            )
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = state.book?.authors?.joinToString(",") ?: "",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Category",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Medium
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if(state.book?.categories?.isNotEmpty() == true){
                    state.book?.categories?.forEach {
                        BookChip {
                            Text(text = it)
                        }
                    }
                } else {
                    Text(
                        text = "no category available",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    )
                }
            }
            Text(
                text = "Description",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = state.book?.description ?: "no description available",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            )
        }
        Button(
            onClick = {

            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(end = 16.dp, start = 16.dp, bottom = 16.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Read Book",
                style = TextStyle(
                    color = Color.White,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}