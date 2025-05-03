package com.capstone.bookshelf.presentation.home_screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Book
import kotlin.math.absoluteValue

@Composable
fun RecentBookCard(
    book: Book,
    pagerState: PagerState,
    pageIndex: Int,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
        .absoluteValue.coerceIn(0f, 1f)
    val density = LocalDensity.current
    val scale = lerp(start = 0.85f, stop = 1f, fraction = 1f - pageOffset)
    val alpha = lerp(start = 0.6f, stop = 1f, fraction = 1f - pageOffset)
    val shadow = lerp(
        start = with(density) { 4.dp.toPx() },
        stop = with(density) { 12.dp.toPx() },
        fraction = 1f - pageOffset
    )
    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onDoubleClick = {
                    onDoubleClick()
                }
            )
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
                this.shadowElevation = shadow
                this.shape = cardShape
                this.clip = true
            }
            .fillMaxWidth()
            .aspectRatio(0.65f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = cardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = if (book.coverImagePath == "error") {
                        R.mipmap.book_cover_not_available
                    } else {
                        book.coverImagePath
                    },
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .clip(cardShape)
                        .background(color = MaterialTheme.colorScheme.surfaceContainer)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = book.title,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Medium
                            ),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = book.authors.joinToString(","),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            progress = { (book.currentChapter + 1).toFloat() / book.totalChapter.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${book.currentChapter + 1} / ${book.totalChapter}",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            ),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}