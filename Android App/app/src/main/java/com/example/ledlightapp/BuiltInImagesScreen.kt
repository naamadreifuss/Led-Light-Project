package com.example.ledlightapp

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color

@Composable
fun BuiltInImagesScreen(
    navController: NavController
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        val context = LocalContext.current

        // Images stored inside the app
        val imageResources = remember {
            listOf(
                R.drawable.duck,
                R.drawable.heart,
                R.drawable.beatlesroad,
                R.drawable.frog,
                R.drawable.technion,
                R.drawable.cat,
                R.drawable.owl,
                R.drawable.dog,
                R.drawable.dolphin,
                R.drawable.squirrel,
                R.drawable.monalisa
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier.height(48.dp)
                )
                // Screen title
                Text(
                    text = "choose image you want"
                )

                Spacer(
                    modifier = Modifier.height(48.dp)
                )

                // Grid of built-in images
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(imageResources) { imageResource ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    // Load the selected built-in image
                                    val bitmap = BitmapFactory.decodeResource(
                                        context.resources,
                                        imageResource
                                    )

                                    if (bitmap != null) {

                                        // Crop the image to the LED matrix aspect ratio
                                        val cropped =
                                            ImageProcessor.cropToMatrixRatio(bitmap)

                                        // Resize the image to exactly 32x16
                                        val resized =
                                            ImageProcessor.resizeBitmap(cropped)

                                        // Convert the 32x16 bitmap into RGB pixels
                                        val pixels =
                                            ImageProcessor.bitmapToPixelList(resized)

                                        // Send the pixels to Firebase
                                        FirebaseManager.sendImagePixels(
                                            pixels = pixels,

                                            onSuccess = {
                                                // Set the current mode to image
                                                FirebaseManager.updateStatus(
                                                    mode = "image",
                                                    content = ""
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "Image sent successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },

                                            onFailure = {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to send image",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )

                                        // Release temporary bitmaps
                                        if (cropped != bitmap) {
                                            cropped.recycle()
                                        }

                                        resized.recycle()
                                        bitmap.recycle()
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = imageResource,
                                contentDescription = "Built-in Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Space for bottom button
                Spacer(
                    modifier = Modifier.height(100.dp)
                )
            }

            // Back button
            Button(
                onClick = {
                    navController.navigate("image") {
                        popUpTo("image") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Back To image mode")
            }
        }
    }
}