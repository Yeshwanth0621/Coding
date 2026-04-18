package com.kkc.clubconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kkc.clubconnect.R
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.notifications.EventRegistrationManager
import com.kkc.clubconnect.ui.components.BackendSetupCard
import com.kkc.clubconnect.ui.components.ClubAvatar
import com.kkc.clubconnect.ui.components.ClubBackdrop
import com.kkc.clubconnect.ui.components.EventArtwork
import com.kkc.clubconnect.ui.components.MessageBanner
import com.kkc.clubconnect.ui.components.SectionHeading
import com.kkc.clubconnect.ui.theme.GlowSecondary
import com.kkc.clubconnect.ui.theme.Ink
import com.kkc.clubconnect.ui.theme.PanelBottom
import com.kkc.clubconnect.ui.theme.PanelTop
import com.kkc.clubconnect.ui.theme.Slate
import com.kkc.clubconnect.ui.theme.SoftLine
import com.kkc.clubconnect.ui.theme.Surface
import com.kkc.clubconnect.ui.theme.Teal
import com.kkc.clubconnect.util.ClubBrandingUtils
import com.kkc.clubconnect.util.DateTimeUtils
import com.kkc.clubconnect.util.UrlUtils
import com.kkc.clubconnect.viewmodel.HomeUiState

@Composable
fun HomeScreen(uiState: HomeUiState) {
    var selectedEvent by remember { mutableStateOf<ClubEvent?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        ClubBackdrop(modifier = Modifier.matchParentSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 108.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                FeedHeaderCard(
                    featuredEvent = uiState.featuredEvent,
                    totalEvents = uiState.events.size,
                )
            }

            if (!uiState.backendConfigured) {
                item {
                    BackendSetupCard(
                        title = "Connect Supabase to go live",
                        subtitle = "Add your Supabase project URL and anon key to turn on club accounts, event publishing, and reminder updates.",
                    )
                }
                return@LazyColumn
            }

            uiState.errorMessage?.let { message ->
                item {
                    MessageBanner(
                        message = message,
                        isError = true,
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    SimpleInfoCard("Pulling the latest club drops...")
                }
                return@LazyColumn
            }

            if (uiState.events.isEmpty()) {
                item {
                    SimpleInfoCard("No club events are live yet. The feed updates automatically as soon as a club posts one.")
                }
            } else {
                items(uiState.events, key = { it.id }) { event ->
                    CompactEventCard(
                        event = event,
                        onOpenDetails = { selectedEvent = event },
                    )
                }
            }
        }
    }

    selectedEvent?.let { event ->
        EventDetailsDialog(
            event = event,
            onDismiss = { selectedEvent = null },
        )
    }
}

@Composable
private fun FeedHeaderCard(
    featuredEvent: ClubEvent?,
    totalEvents: Int,
) {
    ElevatedCard(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelTop)
                .border(1.dp, SoftLine, RoundedCornerShape(30.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(GlowSecondary.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.club_connect_brand_mark),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionHeading(title = "Campus feed")
                    Text(
                        text = "Every club. One live signal.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                }
            }
            Text(
                text = "Scan more events at once. Tap any card for the poster, full details, and registration link.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$totalEvents live updates across CIT",
                    style = MaterialTheme.typography.labelLarge,
                    color = Teal,
                )
                featuredEvent?.let { event ->
                    Text(
                        text = "Spotlight: ${event.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactEventCard(
    event: ClubEvent,
    onOpenDetails: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.clickable(onClick = onOpenDetails),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 98.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorFromBannerHex(event.clubBannerColorHex).copy(alpha = 0.22f),
                            PanelBottom,
                        ),
                    ),
                )
                .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClubAvatar(
                logoUrl = event.clubLogoUrl,
                label = event.clubName.ifBlank { event.title },
                size = 52.dp,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = event.title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (event.featured) {
                        FeedTag("Hot")
                    }
                }
                Text(
                    text = event.clubName.ifBlank { "CIT club" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Teal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = truncateFeedCopy(event.summary.ifBlank { event.description }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = DateTimeUtils.toCardLabel(event.startAtMillis),
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FeedTag(DateTimeUtils.toMonthChip(event.startAtMillis))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Know more",
                        style = MaterialTheme.typography.labelLarge,
                        color = Teal,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowOutward,
                        contentDescription = null,
                        tint = Teal,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedTag(text: String) {
    Box(
        modifier = Modifier
            .background(PanelTop, RoundedCornerShape(100.dp))
            .border(1.dp, SoftLine, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
        )
    }
}

@Composable
private fun SimpleInfoCard(text: String) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBottom)
                .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Slate,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EventDetailsDialog(
    event: ClubEvent,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var notifyOneHourBefore by remember(event.id) {
        mutableStateOf(EventRegistrationManager.isOneHourReminderEnabled(context, event.id))
    }
    var registrationFeedback by remember(event.id) { mutableStateOf<String?>(null) }
    val externalRegistrationUrl = remember(event.registrationLink) { UrlUtils.toExternalRegistrationUrl(event.registrationLink) }
    val isAlreadyRegistered = EventRegistrationManager.isRegistered(context, event.id)

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colorFromBannerHex(event.clubBannerColorHex).copy(alpha = 0.26f),
                                PanelTop,
                            ),
                        ),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ClubAvatar(
                        logoUrl = event.clubLogoUrl,
                        label = event.clubName.ifBlank { event.title },
                        size = 48.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = event.clubName.ifBlank { "CIT Club" },
                            style = MaterialTheme.typography.labelLarge,
                            color = Teal,
                        )
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                EventArtwork(
                    imageUrl = event.imageUrl,
                    title = event.title,
                    height = 176.dp,
                )

                Text(
                    text = event.summary.ifBlank { truncateFeedCopy(event.description) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                )

                DetailMetaRow(
                    icon = Icons.Rounded.CalendarMonth,
                    label = DateTimeUtils.toRange(event.startAtMillis, event.endAtMillis),
                )

                if (event.location.isNotBlank()) {
                    DetailMetaRow(
                        icon = Icons.Rounded.LocationOn,
                        label = event.location,
                    )
                }

                Text(
                    text = event.description.ifBlank { event.summary },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.Switch(
                        checked = notifyOneHourBefore,
                        onCheckedChange = { notifyOneHourBefore = it },
                    )
                    Text(
                        text = "Notify me 1 hour before this event",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                    )
                }
                registrationFeedback?.let { feedback ->
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Teal,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            EventRegistrationManager.registerForEvent(
                                context = context,
                                event = event,
                                oneHourReminderEnabled = notifyOneHourBefore,
                            )
                            registrationFeedback = if (notifyOneHourBefore) {
                                "Registered. You will get a reminder 1 hour before start."
                            } else {
                                "Registered."
                            }
                            externalRegistrationUrl?.let { url ->
                                UrlUtils.openUrl(uriHandler, url)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text(
                            if (isAlreadyRegistered) {
                                "Registered"
                            } else if (externalRegistrationUrl != null) {
                                "Register"
                            } else {
                                "Register for Reminder"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Teal,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
    }
}

private fun truncateFeedCopy(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.length <= 50) {
        return trimmed
    }

    return trimmed.take(50).trimEnd() + "....."
}

private fun colorFromBannerHex(raw: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(ClubBrandingUtils.normalizeBannerColorHex(raw)))
}.getOrDefault(Teal)
