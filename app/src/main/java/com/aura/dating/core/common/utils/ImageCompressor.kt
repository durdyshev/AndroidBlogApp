package com.aura.dating.core.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageCompressor {

    private const val MAX_WIDTH = 1080
    private const val MAX_HEIGHT = 1440
    private const val TARGET_COMPRESSION_QUALITY = 82

    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT,
        quality: Int = TARGET_COMPRESSION_QUALITY
    ): ByteArray {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        requireNotNull(originalBitmap) { "Unable to decode image from uri: $imageUri" }

        val rotatedBitmap = correctOrientation(context, imageUri, originalBitmap)
        val scaledBitmap = scaleBitmapDown(rotatedBitmap, maxWidth, maxHeight)

        val outputStream = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
        } else {
            @Suppress("DEPRECATION")
            scaledBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        }

        if (scaledBitmap != originalBitmap && !scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }
        if (rotatedBitmap != originalBitmap && !rotatedBitmap.isRecycled) {
            rotatedBitmap.recycle()
        }
        if (!originalBitmap.isRecycled) {
            originalBitmap.recycle()
        }

        return outputStream.toByteArray()
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxW && height <= maxH) {
            return bitmap
        }

        val ratio = max(width.toFloat() / maxW, height.toFloat() / maxH)
        val targetWidth = (width / ratio).roundToInt()
        val targetHeight = (height / ratio).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun correctOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(input)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            input.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }
}
