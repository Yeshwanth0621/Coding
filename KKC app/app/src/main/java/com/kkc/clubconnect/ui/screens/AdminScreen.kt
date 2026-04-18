package com.kkc.clubconnect.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kkc.clubconnect.data.ClubSession
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.model.ClubProfileDraft
import com.kkc.clubconnect.model.EventDraft
import com.kkc.clubconnect.ui.components.BackendSetupCard
import com.kkc.clubconnect.ui.components.ClubAvatar
import com.kkc.clubconnect.ui.components.ClubBackdrop
import com.kkc.clubconnect.ui.components.EventEditorDialog
import com.kkc.clubconnect.ui.components.MessageBanner
import com.kkc.clubconnect.ui.components.SectionHeading
import com.kkc.clubconnect.ui.components.clubTextFieldColors
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
import com.kkc.clubconnect.viewmodel.AdminUiState
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    uiState: AdminUiState,
    onSignIn: (String, String) -> Unit,
    onRegisterClub: (String, ClubProfileDraft) -> Unit,
    onSaveClubProfile: (ClubProfileDraft) -> Unit,
    onUploadClubLogo: suspend (Uri) -> Result<String>,
    onUploadEventPoster: suspend (Uri) -> Result<String>,
    onSignOut: () -> Unit,
    onSaveEvent: (String?, EventDraft) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onClearBanner: () -> Unit,
) {
    var editorTarget by remember { mutableStateOf<ClubEvent?>(null) }
    var showNewDialog by remember { mutableStateOf(false) }
    var showProfileEditor by rememberSaveable(uiState.session.uid) { mutableStateOf(false) }

    val ownEvents = remember(uiState.events, uiState.session.uid) {
        uiState.events.filter { event -> event.clubOwnerId == uiState.session.uid }
    }

    if (editorTarget != null || showNewDialog) {
        EventEditorDialog(
            existingEvent = editorTarget,
            onUploadPoster = onUploadEventPoster,
            onDismiss = {
                editorTarget = null
                showNewDialog = false
            },
            onSave = { eventId, draft ->
                onSaveEvent(eventId, draft)
                editorTarget = null
                showNewDialog = false
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        ClubBackdrop(modifier = Modifier.matchParentSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ClubConsoleHeader(
                    session = uiState.session,
                    totalEvents = ownEvents.size,
                )
            }

            if (!uiState.backendConfigured) {
                item {
                    BackendSetupCard(
                        title = "Connect Supabase first",
                        subtitle = "Club access, profiles, and event publishing unlock once your Supabase URL, anon key, and SQL schema are in place.",
                    )
                }
                return@LazyColumn
            }

            if (uiState.isWorking) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    MessageBanner(
                        message = message,
                        isError = true,
                        onDismiss = onClearBanner,
                    )
                }
            }

            uiState.successMessage?.let { message ->
                item {
                    MessageBanner(
                        message = message,
                        isError = false,
                        onDismiss = onClearBanner,
                    )
                }
            }

            when {
                !uiState.session.isSignedIn -> {
                    item {
                        ClubAccessCard(
                            isWorking = uiState.isWorking,
                            onSignIn = onSignIn,
                            onRegisterClub = onRegisterClub,
                        )
                    }
                }

                !uiState.session.hasCompletedSetup -> {
                    item {
                        ClubProfileEditorCard(
                            title = "Finish your club setup",
                            subtitle = "This one-time profile creates the public identity students will see in the feed.",
                            initialDraft = uiState.session.toDraft(),
                            isWorking = uiState.isWorking,
                            submitLabel = "Save club profile",
                            lockClubId = true,
                            showPasswordField = false,
                            canUploadLogo = true,
                            onUploadLogo = onUploadClubLogo,
                            onSubmit = { _, draft -> onSaveClubProfile(draft) },
                        )
                    }
                }

                else -> {
                    item {
                        ClubOverviewCard(
                            session = uiState.session,
                            eventCount = ownEvents.size,
                            onEditProfile = { showProfileEditor = !showProfileEditor },
                            onSignOut = onSignOut,
                        )
                    }

                    if (showProfileEditor) {
                        item {
                            ClubProfileEditorCard(
                                title = "Refresh your club profile",
                                subtitle = "Keep your club name, logo, contact, and description fresh. The club ID stays locked because it doubles as the login handle.",
                                initialDraft = uiState.session.toDraft(),
                                isWorking = uiState.isWorking,
                                submitLabel = "Update profile",
                                lockClubId = true,
                                showPasswordField = false,
                                canUploadLogo = true,
                                onUploadLogo = onUploadClubLogo,
                                onSubmit = { _, draft -> onSaveClubProfile(draft) },
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionHeading(title = "Your club posts")
                            Text(
                                text = "${ownEvents.size} live",
                                style = MaterialTheme.typography.labelLarge,
                                color = Teal,
                            )
                        }
                    }

                    if (ownEvents.isEmpty()) {
                        item {
                            EmptyClubEventsCard()
                        }
                    } else {
                        items(ownEvents, key = { it.id }) { event ->
                            ClubEventConsoleCard(
                                event = event,
                                onEdit = { editorTarget = event },
                                onDelete = { onDeleteEvent(event.id) },
                            )
                        }
                    }
                }
            }
        }

        if (uiState.session.isSignedIn && uiState.session.hasCompletedSetup) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                onClick = { showNewDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Post event") },
            )
        }
    }
}

@Composable
private fun ClubConsoleHeader(
    session: ClubSession,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeading(title = "Club console")
            Text(
                text = "Publish once. Students see it instantly.",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
            )
            Text(
                text = if (session.isSignedIn) {
                    "Signed in as ${session.clubName.ifBlank { session.clubId.ifBlank { "your club" } }}. Manage your identity, post events, and keep registration links fresh."
                } else {
                    "Each club can keep one shared login. First-time setup captures the club name, logo, contact, and description without touching app code."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
            Text(
                text = if (session.hasCompletedSetup) "$totalEvents active posts from your club" else "Ready for multi-club publishing",
                style = MaterialTheme.typography.labelLarge,
                color = Teal,
            )
        }
    }
}

@Composable
private fun ClubAccessCard(
    isWorking: Boolean,
    onSignIn: (String, String) -> Unit,
    onRegisterClub: (String, ClubProfileDraft) -> Unit,
) {
    var showSetup by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBottom)
                .border(1.dp, SoftLine, RoundedCornerShape(28.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeButton(
                    selected = !showSetup,
                    label = "Sign in",
                    onClick = { showSetup = false },
                )
                ModeButton(
                    selected = showSetup,
                    label = "First setup",
                    onClick = { showSetup = true },
                )
            }

            if (showSetup) {
                ClubProfileEditorCard(
                    title = "Create a club access",
                    subtitle = "Set the club ID and password you want your club core team to use, then fill in the public details students will see.",
                    initialDraft = ClubProfileDraft(),
                    isWorking = isWorking,
                    submitLabel = "Create club access",
                    lockClubId = false,
                    showPasswordField = true,
                    canUploadLogo = false,
                    onUploadLogo = null,
                    onSubmit = onRegisterClub,
                )
            } else {
                ClubSignInPanel(
                    isWorking = isWorking,
                    onSignIn = onSignIn,
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                color = if (selected) PanelTop else Surface,
                shape = RoundedCornerShape(100.dp),
            )
            .border(
                width = 1.dp,
                color = if (selected) Teal else SoftLine,
                shape = RoundedCornerShape(100.dp),
            ),
    ) {
        Text(
            text = label,
            color = if (selected) Ink else Slate,
        )
    }
}

@Composable
private fun ClubSignInPanel(
    isWorking: Boolean,
    onSignIn: (String, String) -> Unit,
) {
    var clubId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Login,
                contentDescription = null,
                tint = Teal,
            )
            Text(
                text = "Club sign in",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        }
        Text(
            text = "Use the club ID and password your team shares internally. The app resolves that club ID to the club email you saved during first setup.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate,
        )
        OutlinedTextField(
            value = clubId,
            onValueChange = { clubId = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club ID") },
            singleLine = true,
            colors = clubTextFieldColors(),
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
        )
        Button(
            onClick = { onSignIn(clubId, password) },
            enabled = !isWorking && clubId.isNotBlank() && password.isNotBlank(),
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun ClubProfileEditorCard(
    title: String,
    subtitle: String,
    initialDraft: ClubProfileDraft,
    isWorking: Boolean,
    submitLabel: String,
    lockClubId: Boolean,
    showPasswordField: Boolean,
    canUploadLogo: Boolean,
    onUploadLogo: (suspend (Uri) -> Result<String>)?,
    onSubmit: (String, ClubProfileDraft) -> Unit,
) {
    var clubId by rememberSaveable(initialDraft.clubId, lockClubId) { mutableStateOf(initialDraft.clubId) }
    var password by rememberSaveable(showPasswordField, initialDraft.clubId) { mutableStateOf("") }
    var passwordVisible by rememberSaveable(showPasswordField, initialDraft.clubId) { mutableStateOf(false) }
    var clubName by rememberSaveable(initialDraft.clubName) { mutableStateOf(initialDraft.clubName) }
    var clubLogoUrl by rememberSaveable(initialDraft.clubLogoUrl) { mutableStateOf(initialDraft.clubLogoUrl) }
    var bannerColorHex by rememberSaveable(initialDraft.clubBannerColorHex) {
        mutableStateOf(ClubBrandingUtils.normalizeBannerColorHex(initialDraft.clubBannerColorHex))
    }
    val initialColorChannels = remember(initialDraft.clubBannerColorHex) {
        ClubBrandingUtils.rgbFromBannerColorHex(initialDraft.clubBannerColorHex)
    }
    var redChannel by rememberSaveable(initialDraft.clubBannerColorHex) { mutableStateOf(initialColorChannels.first.toFloat()) }
    var greenChannel by rememberSaveable(initialDraft.clubBannerColorHex) { mutableStateOf(initialColorChannels.second.toFloat()) }
    var blueChannel by rememberSaveable(initialDraft.clubBannerColorHex) { mutableStateOf(initialColorChannels.third.toFloat()) }
    var contactName by rememberSaveable(initialDraft.contactName) { mutableStateOf(initialDraft.contactName) }
    var contactEmail by rememberSaveable(initialDraft.contactEmail) { mutableStateOf(initialDraft.contactEmail) }
    var description by rememberSaveable(initialDraft.description) { mutableStateOf(initialDraft.description) }
    var isUploadingLogo by rememberSaveable { mutableStateOf(false) }
    var uploadFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && onUploadLogo != null) {
            scope.launch {
                isUploadingLogo = true
                uploadFeedback = null
                onUploadLogo(uri)
                    .onSuccess { uploadedUrl ->
                        clubLogoUrl = uploadedUrl
                        uploadFeedback = "Logo uploaded."
                    }
                    .onFailure { error ->
                        uploadFeedback = error.message ?: "Logo upload failed."
                    }
                isUploadingLogo = false
            }
        }
    }

    val canSubmit = clubId.isNotBlank() &&
        clubName.isNotBlank() &&
        contactName.isNotBlank() &&
        contactEmail.isNotBlank() &&
        description.isNotBlank() &&
        (!showPasswordField || password.length >= 6)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClubAvatar(
                logoUrl = clubLogoUrl,
                label = clubName.ifBlank { clubId.ifBlank { "CIT Club" } },
                size = 56.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (canUploadLogo && onUploadLogo != null) {
                    Button(
                        onClick = {
                            logoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        enabled = !isUploadingLogo,
                    ) {
                        Text("Upload logo")
                    }
                } else {
                    Text(
                        text = "Local logo upload unlocks right after the club access is created.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate,
                    )
                }
                if (isUploadingLogo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(start = 2.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Compressing and uploading logo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Teal,
                        )
                    }
                } else {
                    uploadFeedback?.let { feedback ->
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (feedback.contains("failed", ignoreCase = true)) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Teal
                            },
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = clubId,
            onValueChange = { clubId = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club ID") },
            singleLine = true,
            enabled = !lockClubId,
            colors = clubTextFieldColors(),
        )
        if (showPasswordField) {
            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            )
        }
        OutlinedTextField(
            value = clubName,
            onValueChange = { clubName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club name") },
            singleLine = true,
            colors = clubTextFieldColors(),
        )
        OutlinedTextField(
            value = clubLogoUrl,
            onValueChange = { clubLogoUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club logo URL") },
            placeholder = { Text("https://example.com/logo.png") },
            singleLine = true,
            colors = clubTextFieldColors(),
        )
        BannerColorEditor(
            bannerColorHex = bannerColorHex,
            redChannel = redChannel,
            greenChannel = greenChannel,
            blueChannel = blueChannel,
            onPresetSelected = { colorHex ->
                val normalized = ClubBrandingUtils.normalizeBannerColorHex(colorHex)
                val (red, green, blue) = ClubBrandingUtils.rgbFromBannerColorHex(normalized)
                bannerColorHex = normalized
                redChannel = red.toFloat()
                greenChannel = green.toFloat()
                blueChannel = blue.toFloat()
            },
            onChannelChanged = { red, green, blue ->
                redChannel = red
                greenChannel = green
                blueChannel = blue
                bannerColorHex = ClubBrandingUtils.bannerColorHexFromRgb(
                    red = red.toInt(),
                    green = green.toInt(),
                    blue = blue.toInt(),
                )
            },
        )
        OutlinedTextField(
            value = contactName,
            onValueChange = { contactName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club lead / contact name") },
            singleLine = true,
            colors = clubTextFieldColors(),
        )
        OutlinedTextField(
            value = contactEmail,
            onValueChange = { contactEmail = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Club email (used for login)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = clubTextFieldColors(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Short club description") },
            minLines = 3,
            maxLines = 5,
            colors = clubTextFieldColors(),
        )
        Button(
            onClick = {
                onSubmit(
                    password,
                    ClubProfileDraft(
                        clubId = clubId,
                        clubName = clubName,
                        clubLogoUrl = clubLogoUrl,
                        clubBannerColorHex = bannerColorHex,
                        contactName = contactName,
                        contactEmail = contactEmail,
                        description = description,
                    ),
                )
            },
            enabled = !isWorking && canSubmit,
        ) {
            Text(submitLabel)
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Password,
                contentDescription = null,
            )
        },
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = null,
                )
            }
        },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        colors = clubTextFieldColors(),
    )
}

@Composable
private fun ClubOverviewCard(
    session: ClubSession,
    eventCount: Int,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorFromHex(session.clubBannerColorHex).copy(alpha = 0.28f),
                            PanelTop,
                        ),
                    ),
                )
                .border(1.dp, SoftLine, RoundedCornerShape(28.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ClubAvatar(
                    logoUrl = session.clubLogoUrl,
                    label = session.clubName.ifBlank { session.clubId },
                    size = 60.dp,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = session.clubName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                    )
                    Text(
                        text = "@${session.clubId}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Teal,
                    )
                    Text(
                        text = session.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ConsoleMetric(
                    modifier = Modifier.weight(1f),
                    label = "Live posts",
                    value = eventCount.toString(),
                )
                ConsoleMetric(
                    modifier = Modifier.weight(1f),
                    label = "Contact",
                    value = session.contactName.ifBlank { "Club team" },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onEditProfile) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Text(" Edit profile")
                }
                TextButton(onClick = onSignOut) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Text(" Sign out")
                }
            }
        }
    }
}

@Composable
private fun ConsoleMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBottom)
                .border(1.dp, SoftLine, RoundedCornerShape(22.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
        }
    }
}

@Composable
private fun EmptyClubEventsCard() {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelBottom)
                .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Teal,
                )
                Text(
                    text = "Your club is ready to publish",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
            }
            Text(
                text = "Create the first event to place your club in the campus feed. Add a poster URL and registration link if you have them.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
        }
    }
}

@Composable
private fun ClubEventConsoleCard(
    event: ClubEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorFromHex(event.clubBannerColorHex).copy(alpha = 0.22f),
                            PanelBottom,
                        ),
                    ),
                )
                .border(1.dp, SoftLine, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = event.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (event.featured) {
                    SectionHeading(title = "Pinned")
                }
            }

            Text(
                text = DateTimeUtils.toRange(event.startAtMillis, event.endAtMillis),
                style = MaterialTheme.typography.labelLarge,
                color = Ink,
            )
            Text(
                text = event.location.ifBlank { "Location can be announced later" },
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.registrationLink.isNotBlank()) {
                Text(
                    text = if (UrlUtils.isDummyRegistrationUrl(event.registrationLink)) {
                        "Reminder-only registration active"
                    } else {
                        "Registration linked + 1h reminder enabled"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Teal,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Text(" Edit")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                    Text(" Delete")
                }
            }
        }
    }
}

private fun ClubSession.toDraft(): ClubProfileDraft = ClubProfileDraft(
    clubId = clubId,
    clubName = clubName,
    clubLogoUrl = clubLogoUrl,
    clubBannerColorHex = clubBannerColorHex,
    contactName = contactName,
    contactEmail = contactEmail,
    description = description,
)

@Composable
private fun BannerColorEditor(
    bannerColorHex: String,
    redChannel: Float,
    greenChannel: Float,
    blueChannel: Float,
    onPresetSelected: (String) -> Unit,
    onChannelChanged: (Float, Float, Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Club banner color",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colorFromHex(bannerColorHex), CircleShape)
                    .border(1.dp, SoftLine, CircleShape),
            )
            Text(
                text = "$bannerColorHex  (R:${redChannel.toInt()} G:${greenChannel.toInt()} B:${blueChannel.toInt()})",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClubBrandingUtils.commonBannerColors.forEach { presetColor ->
                val normalized = ClubBrandingUtils.normalizeBannerColorHex(presetColor)
                val selected = normalized == ClubBrandingUtils.normalizeBannerColorHex(bannerColorHex)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(colorFromHex(normalized), CircleShape)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Ink else SoftLine,
                            shape = CircleShape,
                        )
                        .clickable { onPresetSelected(normalized) },
                )
            }
        }
        ColorChannelSlider(
            label = "Red",
            value = redChannel,
            onValueChange = { onChannelChanged(it, greenChannel, blueChannel) },
        )
        ColorChannelSlider(
            label = "Green",
            value = greenChannel,
            onValueChange = { onChannelChanged(redChannel, it, blueChannel) },
        )
        ColorChannelSlider(
            label = "Blue",
            value = blueChannel,
            onValueChange = { onChannelChanged(redChannel, greenChannel, it) },
        )
    }
}

@Composable
private fun ColorChannelSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$label: ${value.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
        )
    }
}

private fun colorFromHex(raw: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(ClubBrandingUtils.normalizeBannerColorHex(raw)))
}.getOrDefault(Teal)
