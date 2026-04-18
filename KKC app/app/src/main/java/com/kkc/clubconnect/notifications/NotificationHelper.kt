package com.kkc.clubconnect.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.kkc.clubconnect.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class EventNotificationPayload(
    val id: Int,
    val title: String,
    val eventName: String,
    val eventTimeLabel: String,
    val venue: String = "",
    val clubName: String = "",
    val bodyOverride: String? = null,
    val clubLogoUrl: String = "",
)

object NotificationHelper {

    private const val channelId = "club_event_updates"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showEventNotification(
        context: Context,
        title: String,
        body: String,
    ) {
        val payload = EventNotificationPayload(
            id = title.hashCode(),
            title = title,
            eventName = title,
            eventTimeLabel = body,
            bodyOverride = body,
        )
        val notification = buildNotification(context, payload, largeIcon = null)
        try {
            NotificationManagerCompat.from(context).notify(payload.id, notification)
        } catch (_: SecurityException) {
            // Notification permission may have been denied on Android 13+.
        }
    }

    suspend fun showEventNotification(
        context: Context,
        payload: EventNotificationPayload,
    ) {
        val largeIcon = loadLogoBitmap(
            context = context,
            logoUrl = payload.clubLogoUrl,
        )
        val notification = buildNotification(
            context = context,
            payload = payload,
            largeIcon = largeIcon,
        )
        try {
            NotificationManagerCompat.from(context).notify(payload.id, notification)
        } catch (_: SecurityException) {
            // Notification permission may have been denied on Android 13+.
        }
    }

    private fun buildNotification(
        context: Context,
        payload: EventNotificationPayload,
        largeIcon: Bitmap?,
    ) = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(payload.title)
        .setContentText(payload.eventName)
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(
                payload.bodyOverride ?: buildDetailBody(payload),
            ),
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(createLaunchPendingIntent(context))
        .setLargeIcon(largeIcon)
        .build()

    private fun createLaunchPendingIntent(context: Context): PendingIntent {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            ?: Intent(Intent.ACTION_MAIN).apply {
                `package` = context.packageName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun buildDetailBody(payload: EventNotificationPayload): String = buildString {
        append(payload.eventName)
        append('\n')
        append(payload.eventTimeLabel)
        if (payload.venue.isNotBlank()) {
            append('\n')
            append("Venue: ")
            append(payload.venue)
        }
        if (payload.clubName.isNotBlank()) {
            append('\n')
            append("Club: ")
            append(payload.clubName)
        }
    }

    private suspend fun loadLogoBitmap(
        context: Context,
        logoUrl: String,
    ): Bitmap? = withContext(Dispatchers.IO) {
        val trimmedUrl = logoUrl.trim()
        if (trimmedUrl.isBlank()) {
            return@withContext null
        }

        val request = ImageRequest.Builder(context)
            .data(trimmedUrl)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        result.drawable?.toBitmap()
    }
}
