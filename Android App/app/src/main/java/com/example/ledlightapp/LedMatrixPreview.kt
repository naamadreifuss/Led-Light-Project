package com.example.ledlightapp

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Draws a real 32x16 LED matrix preview using colored squares
@Composable
fun LedMatrixPreview(
    bitmap: Bitmap,
    pixelSize: Int = 12
) {

    Column {

        for (y in 0 until bitmap.height) {

            Row {

                for (x in 0 until bitmap.width) {

                    val pixel =
                        bitmap.getPixel(x, y)

                    val r =
                        (pixel shr 16) and 0xFF

                    val g =
                        (pixel shr 8) and 0xFF

                    val b =
                        pixel and 0xFF

                    Box(
                        modifier = Modifier
                            .size(pixelSize.dp)
                            .background(
                                Color(
                                    red = r,
                                    green = g,
                                    blue = b
                                )
                            )
                    )
                }
            }
        }
    }
}