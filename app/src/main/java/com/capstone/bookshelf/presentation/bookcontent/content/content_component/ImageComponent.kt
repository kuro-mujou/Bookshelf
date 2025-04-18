package com.capstone.bookshelf.presentation.bookcontent.content.content_component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage

@Composable
@UnstableApi
fun ImageComponent(
    content: ImageContent,
) {
    Card(
        modifier = Modifier
            .then(
                if (content.contentState.imagePaddingState)
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                else
                    Modifier
            )
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RectangleShape
    ) {
        AsyncImage(
            model = content.content,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}