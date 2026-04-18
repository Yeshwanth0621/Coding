package com.kkc.clubconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkc.clubconnect.ui.theme.BackdropEnd
import com.kkc.clubconnect.ui.theme.GlowSecondary
import com.kkc.clubconnect.ui.theme.Ink
import com.kkc.clubconnect.ui.theme.PanelTop
import com.kkc.clubconnect.ui.theme.Paper
import com.kkc.clubconnect.ui.theme.Slate
import com.kkc.clubconnect.ui.theme.Teal

@Composable
fun EventArtwork(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(24.dp)
    val trimmedUrl = imageUrl.trim()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(PanelTop, BackdropEnd, GlowSecondary.copy(alpha = 0.8f)),
                ),
            ),
    ) {
        if (trimmedUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(trimmedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "$title artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Paper.copy(alpha = 0.78f)),
                        ),
                    ),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    tint = Teal,
                )
                Text(
                    text = "Poster space",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Text(
                    text = "Admins can add an image URL for this event.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                )
            }
        }
    }
}
