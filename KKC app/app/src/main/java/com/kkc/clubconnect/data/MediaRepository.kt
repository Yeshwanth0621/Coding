package com.kkc.clubconnect.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.kkc.clubconnect.BuildConfig
import com.kkc.clubconnect.backend.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream

class MediaRepository(
    private val backendConfigured: Boolean,
) {

    suspend fun uploadClubLogo(
        context: Context,
        imageUri: Uri,
    ): Result<String> = uploadImage(
        context = context,
        imageUri = imageUri,
        kind = UploadKind.ClubLogo,
    )

    suspend fun uploadEventPoster(
        context: Context,
        imageUri: Uri,
    ): Result<String> = uploadImage(
        context = context,
        imageUri = imageUri,
        kind = UploadKind.EventPoster,
    )

    suspend fun deleteManagedAssetFromUrl(
        context: Context,
        assetUrl: String,
    ): Result<Boolean> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        val path = managedPathFromPublicUrl(assetUrl)
            ?: return Result.success(false)

        return runCatching {
            val client = SupabaseProvider.client(context)
            client.storage.from(bucketName).delete(path)
            true
        }.recoverCatching { error ->
            throw IllegalStateException(error.message ?: "Image cleanup failed.")
        }
    }

    private suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        kind: UploadKind,
    ): Result<String> {
        if (!backendConfigured) {
            return Result.failure(IllegalStateException("Supabase is not configured yet."))
        }

        return runCatching {
            val client = SupabaseProvider.client(context)
            val userId = client.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("Sign in before uploading images.")

            val prepared = prepareUpload(context, imageUri, kind)
            val path = when (kind) {
                UploadKind.ClubLogo -> "$userId/club-logo/current.${prepared.extension}"
                UploadKind.EventPoster -> "$userId/event-posters/${System.currentTimeMillis()}.${prepared.extension}"
            }

            val bucket = client.storage.from(bucketName)
            bucket.upload(path, prepared.bytes, upsert = kind == UploadKind.ClubLogo)
            bucket.publicUrl(path)
        }.recoverCatching { error ->
            throw IllegalStateException(error.message ?: "Image upload failed.")
        }
    }

    private fun prepareUpload(
        context: Context,
        imageUri: Uri,
        kind: UploadKind,
    ): PreparedUpload {
        val bitmap = decodeBitmap(context, imageUri)
            ?: throw IllegalArgumentException("Unable to read the selected image.")
        val resized = resizeBitmap(bitmap, maxDimension = kind.maxDimension)

        val format = if (resized.hasAlpha()) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
        val output = ByteArrayOutputStream()
        val quality = if (format == Bitmap.CompressFormat.PNG) 100 else 82
        resized.compress(format, quality, output)
        val bytes = output.toByteArray()

        if (bytes.size > maxUploadBytes) {
            throw IllegalArgumentException("Pick a smaller image. Compressed uploads should stay under 3 MB.")
        }

        return PreparedUpload(
            bytes = bytes,
            extension = extension,
        )
    }

    private fun decodeBitmap(
        context: Context,
        imageUri: Uri,
    ): Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = false
        }
    } else {
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }

    private fun resizeBitmap(
        bitmap: Bitmap,
        maxDimension: Int,
    ): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= maxDimension) {
            return bitmap
        }

        val scale = maxDimension.toFloat() / largestSide.toFloat()
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private data class PreparedUpload(
        val bytes: ByteArray,
        val extension: String,
    )

    private enum class UploadKind(val maxDimension: Int) {
        ClubLogo(maxDimension = 1024),
        EventPoster(maxDimension = 1600),
    }

    private fun managedPathFromPublicUrl(rawUrl: String): String? {
        val assetUri = Uri.parse(rawUrl.trim())
        val projectUri = Uri.parse(BuildConfig.SUPABASE_URL)
        if (assetUri.scheme != projectUri.scheme || assetUri.authority != projectUri.authority) {
            return null
        }

        val expectedPrefix = "/storage/v1/object/public/$bucketName/"
        val encodedPath = assetUri.encodedPath ?: return null
        if (!encodedPath.startsWith(expectedPrefix)) {
            return null
        }

        return Uri.decode(encodedPath.removePrefix(expectedPrefix)).takeIf { it.isNotBlank() }
    }

    private companion object {
        const val bucketName = "club-media"
        const val maxUploadBytes = 3 * 1024 * 1024
    }
}
