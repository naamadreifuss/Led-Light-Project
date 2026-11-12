package com.example.ledlightapp

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun GalleryImageScreen(navController: NavController) {

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {

        // Selected image from gallery
        var selectedImageUri by remember {
            mutableStateOf<Uri?>(null)
        }

        // Processed LED preview bitmap
        var ledPreviewBitmap by remember {
            mutableStateOf<Bitmap?>(null)
        }

        val context = LocalContext.current

        // Gallery picker
        val imagePickerLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->

                selectedImageUri = uri

                ledPreviewBitmap =
                    uri?.let {
                        ImageProcessor.processImage(
                            context,
                            it
                        )
                    }
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
                Text(
                    text = "choose from your gallery"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Select image button
                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Text("Upload Image From Gallery")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Original image preview
                selectedImageUri?.let { uri ->

                    Text("Original Image")

                    Spacer(modifier = Modifier.height(8.dp))

                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // LED preview
                ledPreviewBitmap?.let { bitmap ->

                    Text("LED Matrix Preview")

                    Spacer(modifier = Modifier.height(12.dp))

                    LedMatrixPreview(
                        bitmap = bitmap,
                        pixelSize = 10
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Send button appears only after image selected
                    Button(
                        onClick = {

                            val pixels =
                                ImageProcessor.bitmapToPixelList(
                                    bitmap
                                )

                            FirebaseManager.sendImagePixels(
                                pixels = pixels,

                                onSuccess = {
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
                        }
                    ) {
                        Text("Send To LED")
                    }
                }

                // Space for bottom back button
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