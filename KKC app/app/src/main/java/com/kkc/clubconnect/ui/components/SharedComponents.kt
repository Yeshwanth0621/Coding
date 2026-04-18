package com.kkc.clubconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kkc.clubconnect.R
import com.kkc.clubconnect.ui.theme.Alert
import com.kkc.clubconnect.ui.theme.GlowSecondary
import com.kkc.clubconnect.ui.theme.Ink
import com.kkc.clubconnect.ui.theme.Mint
import com.kkc.clubconnect.ui.theme.PanelBottom
import com.kkc.clubconnect.ui.theme.PanelTop
import com.kkc.clubconnect.ui.theme.Slate
import com.kkc.clubconnect.ui.theme.SoftLine
import com.kkc.clubconnect.ui.theme.Surface
import com.kkc.clubconnect.ui.theme.Teal

@Composable
fun BackendSetupCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoftLine, RoundedCornerShape(28.dp))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Slate,
            )
            Text(
                text = "Add your Supabase project URL and anon key in gradle.properties, then run the SQL in supabase/schema.sql.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
        }
    }
}

@Composable
fun MessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val accent = if (isError) Alert else Teal

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoftLine, RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
                fontWeight = FontWeight.Medium,
            )
            if (onDismiss != null) {
                TextButton(onClick = { onDismiss() }) {
                    Text("Hide")
                }
            }
        }
    }
}

@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Mint)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = Ink,
    )
}

@Composable
fun ClubAvatar(
    logoUrl: String,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val context = LocalContext.current
    val trimmedUrl = logoUrl.trim()
    val initials = label
        .split(" ")
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank { "CC" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(PanelTop, GlowSecondary.copy(alpha = 0.82f), PanelBottom),
                ),
            )
            .border(1.dp, SoftLine, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (trimmedUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(trimmedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "$label logo",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.club_logo_minimal),
                    contentDescription = "$label placeholder logo",
                    modifier = Modifier
                        .matchParentSize()
                        .padding(10.dp),
                )
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
