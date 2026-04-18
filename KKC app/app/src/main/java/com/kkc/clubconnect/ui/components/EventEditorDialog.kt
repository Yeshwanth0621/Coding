package com.kkc.clubconnect.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kkc.clubconnect.model.ClubEvent
import com.kkc.clubconnect.model.EventDraft
import com.kkc.clubconnect.util.DateTimeUtils
import com.kkc.clubconnect.util.UrlUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@Composable
fun EventEditorDialog(
    existingEvent: ClubEvent?,
    onUploadPoster: suspend (Uri) -> Result<String>,
    onDismiss: () -> Unit,
    onSave: (String?, EventDraft) -> Unit,
) {
    val initialDraft = remember(existingEvent?.id) {
        existingEvent?.let(EventDraft::fromEvent) ?: EventDraft()
    }

    var title by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.title) }
    var summary by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.summary) }
    var description by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.description) }
    var location by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.location) }
    var imageUrl by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.imageUrl) }
    var registrationLink by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.registrationLink) }
    var startAtMillis by rememberSaveable(existingEvent?.id) { mutableLongStateOf(initialDraft.startAtMillis) }
    var endAtMillis by rememberSaveable(existingEvent?.id) { mutableLongStateOf(initialDraft.endAtMillis) }
    var featured by rememberSaveable(existingEvent?.id) { mutableStateOf(initialDraft.featured) }
    var isUploadingPoster by rememberSaveable(existingEvent?.id) { mutableStateOf(false) }
    var uploadFeedback by rememberSaveable(existingEvent?.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                isUploadingPoster = true
                uploadFeedback = null
                onUploadPoster(uri)
                    .onSuccess { uploadedUrl ->
                        imageUrl = uploadedUrl
                        uploadFeedback = "Poster uploaded."
                    }
                    .onFailure { error ->
                        uploadFeedback = error.message ?: "Poster upload failed."
                    }
                isUploadingPoster = false
            }
        }
    }

    val canSave = title.isNotBlank() && summary.isNotBlank() && endAtMillis > startAtMillis

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingEvent == null) "Create event" else "Edit event")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EventArtwork(
                    imageUrl = imageUrl,
                    title = title.ifBlank { "Event poster" },
                    height = 148.dp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            posterPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        enabled = !isUploadingPoster,
                    ) {
                        Text("Upload poster")
                    }
                    if (imageUrl.isNotBlank()) {
                        TextButton(onClick = { imageUrl = "" }) {
                            Text("Clear")
                        }
                    }
                }
                if (isUploadingPoster) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Text(
                            text = "Compressing and uploading poster...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
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
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                    singleLine = true,
                    colors = clubTextFieldColors(),
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Feed summary") },
                    placeholder = { Text("Keep it crisp. The feed trims this around 50 chars.") },
                    maxLines = 2,
                    colors = clubTextFieldColors(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    colors = clubTextFieldColors(),
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Venue or room") },
                    maxLines = 2,
                    colors = clubTextFieldColors(),
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Poster image URL") },
                    placeholder = { Text("Filled automatically after upload, or paste a URL") },
                    singleLine = true,
                    colors = clubTextFieldColors(),
                )
                OutlinedTextField(
                    value = registrationLink,
                    onValueChange = { registrationLink = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Registration link") },
                    placeholder = { Text("Optional. Leave blank to create a reminder-only register action.") },
                    singleLine = true,
                    colors = clubTextFieldColors(),
                )
                if (registrationLink.isBlank()) {
                    Text(
                        text = "No URL added. A built-in placeholder link will still be saved so members can tap Register and get reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (UrlUtils.isDummyRegistrationUrl(registrationLink)) {
                    Text(
                        text = "This event uses the reminder-only registration placeholder link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                DateTimePickerRow(
                    label = "Starts",
                    selectedMillis = startAtMillis,
                    onSelected = { selected ->
                        startAtMillis = selected
                        if (endAtMillis <= selected) {
                            endAtMillis = selected + 2 * 60 * 60 * 1000
                        }
                    },
                )

                DateTimePickerRow(
                    label = "Ends",
                    selectedMillis = endAtMillis,
                    onSelected = { endAtMillis = it },
                )

                ToggleRow(
                    label = "Show this at the top of the member feed",
                    checked = featured,
                    onCheckedChange = { featured = it },
                )

                if (endAtMillis <= startAtMillis) {
                    Text(
                        text = "End time should be after the start time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        existingEvent?.id,
                        EventDraft(
                            title = title,
                            summary = summary,
                            description = description.ifBlank { summary },
                            location = location,
                            imageUrl = imageUrl,
                            registrationLink = registrationLink,
                            notifyOneHourBefore = true,
                            startAtMillis = startAtMillis,
                            endAtMillis = endAtMillis,
                            featured = featured,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DateTimePickerRow(
    label: String,
    selectedMillis: Long,
    onSelected: (Long) -> Unit,
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                context.showDateTimePicker(
                    initialMillis = selectedMillis,
                    onSelected = onSelected,
                )
            },
        ) {
            Text(DateTimeUtils.toFullLabel(selectedMillis))
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

private fun Context.showDateTimePicker(
    initialMillis: Long,
    onSelected: (Long) -> Unit,
) {
    val initial = Instant.ofEpochMilli(initialMillis).atZone(ZoneId.systemDefault())

    DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selected = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    onSelected(selected)
                },
                initial.hour,
                initial.minute,
                DateFormat.is24HourFormat(this),
            ).show()
        },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}
