package com.example.ledlightapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import androidx.core.graphics.get

object ImageProcessor {

    // Converts a gallery Uri into a Bitmap
    fun uriToBitmap(
        context: Context,
        uri: Uri
    ): Bitmap? {

        return try {

            val inputStream =
                context.contentResolver.openInputStream(uri)

            BitmapFactory.decodeStream(inputStream)

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }

    // Crops image to match the LED matrix aspect ratio (32x16 = 2:1)
    fun cropToMatrixRatio(
        bitmap: Bitmap
    ): Bitmap {

        val targetRatio = 2f

        val width = bitmap.width
        val height = bitmap.height

        val currentRatio =
            width.toFloat() / height.toFloat()

        return if (currentRatio > targetRatio) {

            val newWidth =
                (height * targetRatio).toInt()

            val xOffset =
                (width - newWidth) / 2

            Bitmap.createBitmap(
                bitmap,
                xOffset,
                0,
                newWidth,
                height
            )

        } else {

            val newHeight =
                (width / targetRatio).toInt()

            val yOffset =
                (height - newHeight) / 2

            Bitmap.createBitmap(
                bitmap,
                0,
                yOffset,
                width,
                newHeight
            )
        }
    }

    // Resizes image to LED matrix resolution (32x16)
    fun resizeBitmap(
        bitmap: Bitmap,
        width: Int = 32,
        height: Int = 16
    ): Bitmap {

        return bitmap.scale(width, height, false)
    }

    // Full processing pipeline:
    // Uri -> Crop -> Resize -> 32x16 Bitmap
    fun processImage(
        context: Context,
        uri: Uri
    ): Bitmap? {

        val original =
            uriToBitmap(context, uri)
                ?: return null

        val cropped =
            cropToMatrixRatio(original)

        val resized =
            resizeBitmap(cropped)

        if (cropped != original)
            cropped.recycle()

        original.recycle()

        return resized
    }

    // Converts a 32x16 bitmap into a flat RGB pixel list
    fun bitmapToPixelList(
        bitmap: Bitmap
    ): List<Int> {

        val pixels =
            mutableListOf<Int>()

        for (y in 0 until bitmap.height) {

            for (x in 0 until bitmap.width) {

                val pixel =
                    bitmap[x, y]

                pixels.add(
                    pixel and 0x00FFFFFF
                )
            }
        }

        return pixels
    }
}